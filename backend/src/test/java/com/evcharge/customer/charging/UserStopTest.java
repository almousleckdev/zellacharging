package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.ChargerSilence;
import com.evcharge.customer.charging.application.StalledSessionCloser;
import com.evcharge.customer.charging.application.StopCommander;
import com.evcharge.customer.charging.application.UserStop;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.CommandOutcome;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserStopTest {

    private final StopCommander stopper = mock(StopCommander.class);
    private final ChargerSilence silence = mock(ChargerSilence.class);
    private final StalledSessionCloser closer = mock(StalledSessionCloser.class);
    private final UserStop userStop = new UserStop(stopper, silence, closer);
    private final ChargingSession session = new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1,
            "Z-X", SessionState.ACTIVE, null, 7, Instant.now(), null, null, null, "XAF", 0, null, Instant.now(),
            Instant.now());

    @Test
    void anAcceptedStopClosesNothingByItself() {
        when(stopper.issue(session)).thenReturn(CommandOutcome.success());

        userStop.perform(session);

        verify(closer, never()).closeLostCharger(any());
    }

    @Test
    void anUnreachableChargerThatStoppedReportingIsClosedAsInterrupted() {
        when(stopper.issue(session)).thenReturn(CommandOutcome.refused("CHARGER_UNREACHABLE", null));
        when(silence.confirmed(session, Duration.ofSeconds(20))).thenReturn(true);

        userStop.perform(session);

        verify(closer).closeLostCharger(session);
    }

    @Test
    void aTimedOutStopCallOnASilentChargerIsClosedAsInterrupted() {
        when(stopper.issue(session))
                .thenThrow(new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "CHARGING_UNAVAILABLE", "x"));
        when(silence.confirmed(session, Duration.ofSeconds(20))).thenReturn(true);

        userStop.perform(session);

        verify(closer).closeLostCharger(session);
    }

    @Test
    void anUnreachableChargerStillReportingIsNotClosed() {
        when(stopper.issue(session)).thenReturn(CommandOutcome.refused("CHARGER_TIMEOUT", null));
        when(silence.confirmed(session, Duration.ofSeconds(20))).thenReturn(false);

        assertThatThrownBy(() -> userStop.perform(session))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo("STOP_REFUSED"));
        verify(closer, never()).closeLostCharger(any());
    }

    @Test
    void aRefusalWithoutAReasonFromASilentChargerIsClosedAsInterrupted() {
        when(stopper.issue(session)).thenReturn(CommandOutcome.refused("CHARGER_REFUSED", null));
        when(silence.confirmed(session, Duration.ofSeconds(20))).thenReturn(true);

        userStop.perform(session);

        verify(closer).closeLostCharger(session);
    }

    @Test
    void aRefusalFromAnAliveChargerIsReportedAsIs() {
        when(stopper.issue(session)).thenReturn(CommandOutcome.refused("CHARGER_REFUSED", null));

        assertThatThrownBy(() -> userStop.perform(session)).isInstanceOf(ApiException.class);
        verify(closer, never()).closeLostCharger(any());
    }
}
