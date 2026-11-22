package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.StartableConnectors;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.CommandOutcome;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.AuditTrail;
import com.evcharge.customer.shared.CustomerStanding;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {

    private static final int HISTORY_LIMIT = 100;

    private final SessionRepository sessions;
    private final IdTagService idTags;
    private final SessionRefresher refresher;
    private final StartableConnectors connectors;
    private final IdempotentStarts starts;
    private final SessionSettlement settlement;
    private final UserStop userStop;
    private final StartCommander starter;
    private final StartFunding funding;
    private final Clock clock;
    private final AuditTrail audit;
    private final CustomerStanding standing;

    public SessionService(SessionRepository sessions, IdTagService idTags,
                          SessionRefresher refresher, StartableConnectors connectors, IdempotentStarts starts,
                          SessionSettlement settlement,
                          UserStop userStop, StartCommander starter, StartFunding funding, Clock clock,
                          AuditTrail audit, CustomerStanding standing) {
        this.sessions = sessions;
        this.idTags = idTags;
        this.refresher = refresher;
        this.connectors = connectors;
        this.starts = starts;
        this.settlement = settlement;
        this.userStop = userStop;
        this.starter = starter;
        this.funding = funding;
        this.clock = clock;
        this.audit = audit;
        this.standing = standing;
    }

    public ChargingSession start(UUID customerId, String stationId, int connectorId, long amountMinor,
                                 String idempotencyKey) {
        standing.requireActive(customerId);
        Optional<ChargingSession> earlier = starts.earlier(customerId, idempotencyKey, stationId, connectorId,
                amountMinor);
        if (earlier.isPresent()) {
            return earlier.get();
        }
        if (sessions.findLive(customerId).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "CONNECTOR_OR_SESSION_BUSY",
                    "A charge is already in progress");
        }
        connectors.require(stationId, connectorId);
        String idTag = idTags.ensure(customerId);
        UUID sessionId = UUID.randomUUID();
        ChargingSession session = ChargingSession.requested(sessionId, customerId, stationId, connectorId, idTag,
                clock.instant());
        long budget = funding.fund(customerId, sessionId, amountMinor, () -> sessions.insert(session, idempotencyKey));
        audit.record("SESSION_START_REQUESTED", customerId, sessionId.toString(), stationId + "#" + connectorId
                + " budget=" + budget);
        return dispatchStart(session);
    }

    private ChargingSession dispatchStart(ChargingSession requested) {
        StartCommander.Started started;
        try {
            started = starter.issue(requested);
        } catch (RuntimeException e) {
            fail(requested, "CHARGING_UNAVAILABLE");
            throw e;
        }
        ChargingSession session = started.session();
        CommandOutcome outcome = started.outcome();
        boolean uncertain = !outcome.accepted() && "CHARGER_TIMEOUT".equals(outcome.reasonCode());
        if (outcome.accepted() || uncertain) {
            ChargingSession next = session.withState(SessionState.START_ACCEPTED, null, clock.instant());
            sessions.update(next);
            return next;
        }
        return fail(session, outcome.reasonCode());
    }

    private ChargingSession fail(ChargingSession session, String code) {
        ChargingSession failed = session.withState(SessionState.FAILED, code, clock.instant());
        sessions.update(failed);
        settlement.settle(failed);
        return failed;
    }

    public ChargingSession stop(UUID customerId, UUID sessionId) {
        ChargingSession session = refresher.refresh(owned(customerId, sessionId));
        if (session.state() == SessionState.START_ACCEPTED) {
            return cancelWaiting(customerId, session);
        }
        if (session.state() != SessionState.ACTIVE) {
            throw new ApiException(HttpStatus.CONFLICT, "SESSION_NOT_ACTIVE", "No active charge to stop");
        }
        audit.record("SESSION_STOP_REQUESTED", customerId, sessionId.toString(), null);
        userStop.perform(session);
        return owned(customerId, sessionId);
    }

    private ChargingSession cancelWaiting(UUID customerId, ChargingSession session) {
        audit.record("SESSION_CANCEL_REQUESTED", customerId, session.id().toString(), null);
        userStop.releaseWaitingCharger(session);
        return fail(session, "CANCELLED");
    }

    public ChargingSession get(UUID customerId, UUID sessionId) {
        return refresher.refresh(owned(customerId, sessionId));
    }

    public Optional<ChargingSession> active(UUID customerId) {
        return sessions.findLive(customerId).map(refresher::refresh).filter(s -> s.state().isLive());
    }

    public List<ChargingSession> history(UUID customerId) {
        return sessions.history(customerId, HISTORY_LIMIT).stream().map(refresher::refresh).toList();
    }

    private ChargingSession owned(UUID customerId, UUID sessionId) {
        return sessions.findOwned(sessionId, customerId).orElseThrow(
                () -> new ApiException(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", "Session not found"));
    }

    public void hide(UUID customerId, UUID sessionId) {
        ChargingSession session = owned(customerId, sessionId);
        if (session.state().isLive()) {
            throw new ApiException(HttpStatus.CONFLICT, "SESSION_STILL_ACTIVE", "A charge in progress cannot be deleted");
        }
        sessions.hide(sessionId, customerId, clock.instant());
    }
}
