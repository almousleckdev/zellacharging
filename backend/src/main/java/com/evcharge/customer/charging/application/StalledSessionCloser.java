package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.application.port.SessionTelemetry;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.SessionProgress;
import com.evcharge.customer.charging.domain.SessionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

@Component
public class StalledSessionCloser {

    private static final Logger log = LoggerFactory.getLogger(StalledSessionCloser.class);

    private final SessionTelemetry telemetry;
    private final SessionRepository sessions;
    private final SessionCostEstimator estimator;
    private final SessionSettlement settlement;
    private final StopCommander stopper;
    private final StallSettings settings;
    private final Clock clock;

    public StalledSessionCloser(SessionTelemetry telemetry, SessionRepository sessions,
                                SessionCostEstimator estimator, SessionSettlement settlement,
                                StopCommander stopper, StallSettings settings, Clock clock) {
        this.telemetry = telemetry;
        this.sessions = sessions;
        this.estimator = estimator;
        this.settlement = settlement;
        this.stopper = stopper;
        this.settings = settings;
        this.clock = clock;
    }

    public boolean closeIfStalled(ChargingSession session) {
        boolean running = session.state() == SessionState.ACTIVE || session.state() == SessionState.STOPPING;
        Instant now = clock.instant();
        SessionProgress progress = telemetry.progressOf(session.id());
        if (!running || !lastMoved(session, progress).plus(settings.after()).isBefore(now)) {
            return false;
        }
        BigDecimal delivered = progress.energyKwh() == null ? BigDecimal.ZERO : progress.energyKwh();
        log.warn("ALERT session {} made no progress since {}: closing it, billing {} kWh and returning the rest",
                session.id(), lastMoved(session, progress), delivered);
        stopQuietly(session);
        ChargingSession closed = priced(session.withEnergy(delivered, now)
                .withState(SessionState.ABANDONED, "STALLED", now), delivered, now);
        sessions.update(closed);
        settlement.settle(closed);
        return true;
    }

    private void stopQuietly(ChargingSession session) {
        try {
            stopper.issue(session);
        } catch (RuntimeException e) {
            log.warn("Stop command for stalled session {} could not be sent", session.id(), e);
        }
    }

    private ChargingSession priced(ChargingSession session, BigDecimal delivered, Instant now) {
        return estimator.forEnergy(session, delivered)
                .map(cost -> session.withCost(cost.minor(), cost.currency(), cost.decimals(), now))
                .orElse(session);
    }

    private static Instant lastMoved(ChargingSession session, SessionProgress progress) {
        if (progress.at() != null) {
            return progress.at();
        }
        return session.startedAt() != null ? session.startedAt() : session.createdAt();
    }
}
