package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.CommandOutcome;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.wallet.WalletFunding;
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
    private final ChargingGateway gateway;
    private final IdTagService idTags;
    private final SessionReconciler reconciler;
    private final SessionSettlement settlement;
    private final StopCommander stopper;
    private final WalletFunding funding;
    private final Clock clock;

    public SessionService(SessionRepository sessions, ChargingGateway gateway, IdTagService idTags,
                          SessionReconciler reconciler, SessionSettlement settlement, StopCommander stopper,
                          WalletFunding funding, Clock clock) {
        this.sessions = sessions;
        this.gateway = gateway;
        this.idTags = idTags;
        this.reconciler = reconciler;
        this.settlement = settlement;
        this.stopper = stopper;
        this.funding = funding;
        this.clock = clock;
    }

    public ChargingSession start(UUID customerId, String stationId, int connectorId, long amountMinor) {
        if (sessions.findLive(customerId).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "CONNECTOR_OR_SESSION_BUSY",
                    "A charge is already in progress");
        }
        String idTag = idTags.ensure(customerId);
        UUID sessionId = UUID.randomUUID();
        funding.reserve(customerId, sessionId, amountMinor);
        ChargingSession session = ChargingSession.requested(sessionId, customerId, stationId, connectorId, idTag,
                clock.instant());
        try {
            sessions.insert(session);
        } catch (RuntimeException e) {
            funding.release(sessionId);
            throw e;
        }
        return dispatchStart(session);
    }

    private ChargingSession dispatchStart(ChargingSession session) {
        CommandOutcome outcome;
        try {
            outcome = gateway.start(session.stationId(), session.connectorId(), session.idTag(),
                    "start:" + session.id());
        } catch (RuntimeException e) {
            fail(session, "CHARGING_UNAVAILABLE");
            throw e;
        }
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
        ChargingSession session = reconciler.reconcile(owned(customerId, sessionId));
        if (session.state() != SessionState.ACTIVE) {
            throw new ApiException(HttpStatus.CONFLICT, "SESSION_NOT_ACTIVE", "No active charge to stop");
        }
        if (!stopper.issue(session).accepted()) {
            throw new ApiException(HttpStatus.CONFLICT, "STOP_REFUSED", "The charger refused to stop");
        }
        return owned(customerId, sessionId);
    }

    public ChargingSession get(UUID customerId, UUID sessionId) {
        return reconciler.reconcile(owned(customerId, sessionId));
    }

    public Optional<ChargingSession> active(UUID customerId) {
        return sessions.findLive(customerId).map(reconciler::reconcile).filter(s -> s.state().isLive());
    }

    public List<ChargingSession> history(UUID customerId) {
        return sessions.history(customerId, HISTORY_LIMIT).stream().map(reconciler::reconcile).toList();
    }

    private ChargingSession owned(UUID customerId, UUID sessionId) {
        return sessions.findOwned(sessionId, customerId).orElseThrow(
                () -> new ApiException(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", "Session not found"));
    }
}
