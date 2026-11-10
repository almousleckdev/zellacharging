package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.EarlyStopRetry;
import com.evcharge.customer.charging.application.StartCommander;
import com.evcharge.customer.charging.application.port.StartAttempts;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.CommandOutcome;
import com.evcharge.customer.charging.domain.SessionState;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EarlyStopRetryTest {

    private static final Instant NOW = Instant.parse("2026-11-10T08:08:40Z");
    private static final Instant STARTED = NOW.minusSeconds(20);

    private final StartCommander starter = mock(StartCommander.class);
    private final StartAttempts attempts = mock(StartAttempts.class);
    private final EarlyStopRetry retry = new EarlyStopRetry(starter, attempts, Clock.fixed(NOW, ZoneOffset.UTC));

    private ChargingSession session(SessionState state, String reason, String kwh, Instant stopped) {
        return new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1, "Z-A", state, null, 824,
                STARTED, stopped, new BigDecimal(kwh), null, null, null, reason, NOW.minusSeconds(40), NOW);
    }

    private ChargingSession running() {
        return session(SessionState.ACTIVE, null, "0", null);
    }

    private ChargingSession endedBy(String reason, String kwh) {
        return session(SessionState.COMPLETED, reason, kwh, NOW.minusSeconds(20));
    }

    private void startIsAccepted(ChargingSession before) {
        when(starter.issue(any())).thenReturn(new StartCommander.Started(before, CommandOutcome.success()));
    }

    @Test
    void aChargeTheChargerEndedWithinSecondsWithNoEnergyIsStartedAgain() {
        ChargingSession before = running();
        startIsAccepted(before);
        when(attempts.retired(before.id())).thenReturn(Set.of());

        ChargingSession next = retry.apply(before, endedBy("Other", "0"));

        assertThat(next.state()).isEqualTo(SessionState.START_ACCEPTED);
        assertThat(next.transactionId()).isNull();
        assertThat(next.startedAt()).isNull();
        verify(attempts).retire(before.id(), 824);
    }

    @Test
    void theChargerGetsTimeToSettleBeforeTheNewStart() {
        ChargingSession before = running();
        ChargingSession justStopped = session(SessionState.COMPLETED, "Other", "0.000", NOW.minusSeconds(5));

        assertThat(retry.apply(before, justStopped)).isSameAs(before);

        verify(starter, never()).issue(any());
    }

    @Test
    void afterTwoRestartsTheChargeFailsWithAClearCodeAndNothingIsCharged() {
        ChargingSession before = running();
        when(attempts.retired(before.id())).thenReturn(Set.of(822, 823));

        ChargingSession next = retry.apply(before, endedBy("Other", "0"));

        assertThat(next.state()).isEqualTo(SessionState.FAILED);
        assertThat(next.failureCode()).isEqualTo("CHARGER_STOPPED_EARLY");
        verify(starter, never()).issue(any());
    }

    @Test
    void aChargeTheCustomerStoppedIsNeverRestarted() {
        ChargingSession before = running();
        ChargingSession ended = endedBy("Remote", "0");

        assertThat(retry.apply(before, ended)).isEqualTo(ended);

        verify(starter, never()).issue(any());
    }

    @Test
    void aChargeThatDeliveredEnergyIsNeverRestarted() {
        ChargingSession before = running();
        ChargingSession ended = endedBy("Other", "1.2");

        assertThat(retry.apply(before, ended)).isEqualTo(ended);

        verify(starter, never()).issue(any());
    }

    @Test
    void aChargeThatRanForMinutesIsNeverRestarted() {
        ChargingSession before = running();
        ChargingSession ended = new ChargingSession(before.id(), before.customerId(), "CP-1", 1, "Z-A",
                SessionState.COMPLETED, null, 824, NOW.minusSeconds(300), NOW.minusSeconds(20), BigDecimal.ZERO, null,
                null, null, "Other", before.createdAt(), NOW);

        assertThat(retry.apply(before, ended)).isEqualTo(ended);
    }

    @Test
    void whenTheChargerRefusesTheNewStartTheChargeFailsWithTheClearCode() {
        ChargingSession before = running();
        when(attempts.retired(before.id())).thenReturn(Set.of());
        when(starter.issue(any())).thenReturn(new StartCommander.Started(before,
                CommandOutcome.refused("CHARGER_BUSY", null)));

        ChargingSession next = retry.apply(before, endedBy("Other", "0"));

        assertThat(next.state()).isEqualTo(SessionState.FAILED);
        assertThat(next.failureCode()).isEqualTo("CHARGER_STOPPED_EARLY");
        verify(attempts, never()).retire(any(), eq(824));
    }
}
