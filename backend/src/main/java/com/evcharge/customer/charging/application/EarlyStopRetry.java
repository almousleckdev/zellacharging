package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.StartAttempts;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.SessionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component
public class EarlyStopRetry {

    private static final Logger log = LoggerFactory.getLogger(EarlyStopRetry.class);
    private static final String CHARGER_REASON = "Other";
    private static final String FAILURE = "CHARGER_STOPPED_EARLY";
    private static final Duration EARLY = Duration.ofSeconds(90);
    private static final Duration SETTLE = Duration.ofSeconds(15);
    private static final int MAX_RESTARTS = 2;

    private final StartCommander starter;
    private final StartAttempts attempts;
    private final Clock clock;

    public EarlyStopRetry(StartCommander starter, StartAttempts attempts, Clock clock) {
        this.starter = starter;
        this.attempts = attempts;
        this.clock = clock;
    }

    public ChargingSession apply(ChargingSession before, ChargingSession after) {
        if (!stoppedEarlyByCharger(before, after)) {
            return after;
        }
        Instant now = clock.instant();
        if (after.stoppedAt().plus(SETTLE).isAfter(now)) {
            return before;
        }
        if (attempts.retired(before.id()).size() < MAX_RESTARTS) {
            StartCommander.Started started = starter.issue(before);
            if (started.outcome().accepted()) {
                attempts.retire(before.id(), after.transactionId());
                log.warn("ALERT session {} was stopped by the charger after seconds with no energy: started again",
                        before.id());
                return started.session().restarted(now);
            }
        }
        log.warn("ALERT session {} keeps being stopped by the charger: failing it without charge", before.id());
        return after.withState(SessionState.FAILED, FAILURE, now);
    }

    private static boolean stoppedEarlyByCharger(ChargingSession before, ChargingSession after) {
        boolean waiting = before.state() == SessionState.ACTIVE || before.state() == SessionState.START_ACCEPTED;
        boolean fromCharger = after.state() == SessionState.COMPLETED && CHARGER_REASON.equals(after.stopReason());
        return waiting && fromCharger && after.transactionId() != null && noEnergy(after) && brief(after);
    }

    private static boolean noEnergy(ChargingSession session) {
        return session.energyKwh() == null || session.measuredNegligibleEnergy();
    }

    private static boolean brief(ChargingSession session) {
        return session.startedAt() != null && session.stoppedAt() != null
                && Duration.between(session.startedAt(), session.stoppedAt()).compareTo(EARLY) < 0;
    }
}
