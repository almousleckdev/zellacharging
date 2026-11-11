package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.application.port.SessionTelemetry;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.RemoteSession;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Component
public class SessionReconciler {

    private static final Logger log = LoggerFactory.getLogger(SessionReconciler.class);

    private final RemoteSessionLookup lookup;
    private final SessionRepository sessions;
    private final SessionTelemetry telemetry;
    private final SessionSettings settings;
    private final SessionSettlement settlement;
    private final SessionCostEstimator estimator;
    private final EarlyStopRetry earlyStop;
    private final Clock clock;

    public SessionReconciler(RemoteSessionLookup lookup, SessionRepository sessions,
                             SessionTelemetry telemetry, SessionSettings settings, SessionSettlement settlement,
                             SessionCostEstimator estimator, EarlyStopRetry earlyStop, Clock clock) {
        this.lookup = lookup;
        this.sessions = sessions;
        this.telemetry = telemetry;
        this.settings = settings;
        this.settlement = settlement;
        this.estimator = estimator;
        this.earlyStop = earlyStop;
        this.clock = clock;
    }

    public ChargingSession reconcile(ChargingSession session) {
        if (!session.state().isLive() || session.state() == SessionState.REQUESTED) {
            return session;
        }
        Instant now = clock.instant();
        Optional<RemoteSession> remote;
        try {
            remote = lookup.find(session);
        } catch (ApiException e) {
            if (e.status() == HttpStatus.SERVICE_UNAVAILABLE) {
                return session;
            }
            throw e;
        }
        rememberPrice(session, remote);
        rememberBattery(session, remote);
        ChargingSession observed = remote
                .map(found -> priced(withMeasuredEnergy(session.withRemote(found, nextState(session, found), now)), now))
                .orElseGet(() -> expireIfStale(session, now));
        ChargingSession next = earlyStop.apply(session, observed);
        if (!next.equals(session)) {
            sessions.update(next);
            logTransition(session, next);
            settlement.onChange(session, next);
        }
        return next;
    }

    private void logTransition(ChargingSession before, ChargingSession after) {
        if (before.state() != after.state()) {
            log.info("Session {} {} -> {} (transaction {}, {} kWh)", after.id(), before.state(), after.state(),
                    after.transactionId(), after.energyKwh());
        }
    }

    private void rememberPrice(ChargingSession session, Optional<RemoteSession> remote) {
        remote.map(RemoteSession::pricePerKwhMinor).filter(price -> price > 0)
                .ifPresent(price -> telemetry.recordPrice(session.id(), price));
    }

    private void rememberBattery(ChargingSession session, Optional<RemoteSession> remote) {
        remote.ifPresent(found -> telemetry.recordBatteryLevels(session.id(), found.socStartPercent(),
                found.socEndPercent()));
    }

    private ChargingSession withMeasuredEnergy(ChargingSession session) {
        boolean finished = session.state() == SessionState.COMPLETED || session.state() == SessionState.ABANDONED;
        if (!finished || session.energyKwh() != null) {
            return session;
        }
        BigDecimal measured = telemetry.progressOf(session.id()).energyKwh();
        return measured == null ? session : session.withMeasuredEnergy(measured);
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
