package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.CommandOutcome;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class UserStop {

    private static final Duration QUIET_FOR = Duration.ofSeconds(20);

    private final StopCommander stopper;
    private final ChargerSilence silence;
    private final StalledSessionCloser closer;

    public UserStop(StopCommander stopper, ChargerSilence silence, StalledSessionCloser closer) {
        this.stopper = stopper;
        this.silence = silence;
        this.closer = closer;
    }

    public void perform(ChargingSession session) {
        CommandOutcome outcome = attempt(session);
        if (outcome.accepted()) {
            return;
        }
        if (silence.confirmed(session, QUIET_FOR)) {
            closer.closeLostCharger(session);
            return;
        }
        throw new ApiException(HttpStatus.CONFLICT, "STOP_REFUSED", "The charger refused to stop");
    }

    public void releaseWaitingCharger(ChargingSession session) {
        try {
            stopper.issue(session);
        } catch (RuntimeException e) {
            return;
        }
    }

    private CommandOutcome attempt(ChargingSession session) {
        try {
            return stopper.issue(session);
        } catch (ApiException e) {
            if (e.status() != HttpStatus.SERVICE_UNAVAILABLE) {
                throw e;
            }
            return CommandOutcome.refused("CHARGER_UNREACHABLE", null);
        }
    }
}
