package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.RemoteSession;
import com.evcharge.customer.charging.domain.SessionState;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;

@Component
public class SessionReconciler {

    private static final long CLOCK_SKEW_SECONDS = 120;

    private final ChargingGateway gateway;
    private final SessionRepository sessions;
    private final SessionSettings settings;
    private final SessionSettlement settlement;
    private final SessionCostEstimator estimator;
    private final Clock clock;

    public SessionReconciler(ChargingGateway gateway, SessionRepository sessions,
                             SessionSettings settings, SessionSettlement settlement,
                             SessionCostEstimator estimator, Clock clock) {
        this.gateway = gateway;
        this.sessions = sessions;
        this.settings = settings;
        this.settlement = settlement;
        this.estimator = estimator;
        this.clock = clock;
    }

    public ChargingSession reconcile(ChargingSession session) {
        if (!session.state().isLive() || session.state() == SessionState.REQUESTED) {
            return session;
        }
        Instant now = clock.instant();
        Optional<RemoteSession> remote = findRemote(session);
        ChargingSession next = remote
                .map(found -> priced(session.withRemote(found, nextState(session, found), now), now))
                .orElseGet(() -> expireIfStale(session, now));
        if (!next.equals(session)) {
            sessions.update(next);
            settlement.settle(next);
        }
        return next;
    }

    private ChargingSession priced(ChargingSession session, Instant now) {
        boolean finished = session.state() == SessionState.COMPLETED || session.state() == SessionState.ABANDONED;
        if (!finished) {
            return session;
        }
        return estimator.finalCost(session)
                .map(cost -> session.withCost(cost.minor(), cost.currency(), cost.decimals(), now))
                .orElse(session);
    }

    private Optional<RemoteSession> findRemote(ChargingSession session) {
        Instant earliest = session.createdAt().minusSeconds(CLOCK_SKEW_SECONDS);
        return gateway.sessionsOf(session.idTag()).stream()
                .filter(remote -> remote.stationId().equals(session.stationId())
                        && remote.connectorId() == session.connectorId())
                .filter(remote -> remote.startedAt() != null && !remote.startedAt().isBefore(earliest))
                .max(Comparator.comparing(RemoteSession::startedAt));
    }

    private SessionState nextState(ChargingSession session, RemoteSession remote) {
        SessionState mapped = remote.toState();
        boolean stillRunning = mapped == SessionState.ACTIVE;
        return stillRunning && session.state() == SessionState.STOPPING ? SessionState.STOPPING : mapped;
    }

    private ChargingSession expireIfStale(ChargingSession session, Instant now) {
        boolean waiting = session.state() == SessionState.START_ACCEPTED;
        boolean stale = session.createdAt().plus(settings.plugInTimeout()).isBefore(now);
        return waiting && stale ? session.withState(SessionState.FAILED, "START_TIMEOUT", now) : session;
    }
}
