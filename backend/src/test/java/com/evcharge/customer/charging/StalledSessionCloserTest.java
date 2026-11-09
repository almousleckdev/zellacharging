package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.ChargerSilence;
import com.evcharge.customer.charging.application.SessionCostEstimator;
import com.evcharge.customer.charging.application.SessionSettlement;
import com.evcharge.customer.charging.application.StallSettings;
import com.evcharge.customer.charging.application.StalledSessionCloser;
import com.evcharge.customer.charging.application.StopCommander;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.application.port.SessionTelemetry;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.SessionProgress;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.Money;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StalledSessionCloserTest {

    private static final Instant NOW = Instant.parse("2026-10-28T12:00:00Z");

    private final SessionTelemetry telemetry = mock(SessionTelemetry.class);
    private final SessionRepository sessions = mock(SessionRepository.class);
    private final SessionCostEstimator estimator = mock(SessionCostEstimator.class);
    private final SessionSettlement settlement = mock(SessionSettlement.class);
    private final StopCommander stopper = mock(StopCommander.class);
    private final ChargerSilence silence = mock(ChargerSilence.class);
    private final StalledSessionCloser closer = new StalledSessionCloser(telemetry, sessions, estimator,
            settlement, stopper, silence, new StallSettings(Duration.ofMinutes(20), Duration.ofSeconds(90)),
            Clock.fixed(NOW, ZoneOffset.UTC));

    private ChargingSession active() {
        Instant started = NOW.minus(Duration.ofHours(1));
        return new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1, "Z-X", SessionState.ACTIVE,
                null, 7, started, null, null, null, "XAF", 0, null, started, started);
    }

    @Test
    void aSessionThatStoppedProgressingIsClosedAndBilledForTheEnergyDelivered() {
        ChargingSession session = active();
        when(telemetry.progressOf(session.id()))
                .thenReturn(new SessionProgress(new BigDecimal("4.2"), NOW.minus(Duration.ofMinutes(25))));
        when(estimator.forEnergy(any(), any())).thenReturn(Optional.of(new Money(630, "XAF", 0)));

        assertThat(closer.closeIfStalled(session)).isTrue();

        ArgumentCaptor<ChargingSession> closed = ArgumentCaptor.forClass(ChargingSession.class);
        verify(settlement).settle(closed.capture());
        assertThat(closed.getValue().state()).isEqualTo(SessionState.ABANDONED);
        assertThat(closed.getValue().failureCode()).isEqualTo("STALLED");
        assertThat(closed.getValue().costMinor()).isEqualTo(630);
        assertThat(closed.getValue().energyKwh()).isEqualByComparingTo("4.2");
    }

    @Test
    void aSessionThatNeverDeliveredEnergyIsBilledNothing() {
        ChargingSession session = active();
        when(telemetry.progressOf(session.id())).thenReturn(SessionProgress.none());
        when(estimator.forEnergy(any(), any())).thenReturn(Optional.of(new Money(0, "XAF", 0)));

        assertThat(closer.closeIfStalled(session)).isTrue();

        ArgumentCaptor<ChargingSession> closed = ArgumentCaptor.forClass(ChargingSession.class);
        verify(settlement).settle(closed.capture());
        assertThat(closed.getValue().costMinor()).isZero();
    }

    @Test
    void aSessionStillProgressingIsLeftAlone() {
        ChargingSession session = active();
        when(telemetry.progressOf(session.id()))
                .thenReturn(new SessionProgress(new BigDecimal("4.2"), NOW.minus(Duration.ofMinutes(2))));

        assertThat(closer.closeIfStalled(session)).isFalse();

        verify(settlement, never()).settle(any());
    }

    @Test
    void aSessionThatIsNotRunningIsLeftAlone() {
        ChargingSession waiting = active().withState(SessionState.START_ACCEPTED, null, NOW);

        assertThat(closer.closeIfStalled(waiting)).isFalse();

        verify(settlement, never()).settle(any());
    }

    @Test
    void aChargerThatWentSilentClosesTheChargeAtOnceAndBillsOnlyTheEnergyDelivered() {
        ChargingSession session = active();
        when(silence.confirmed(session)).thenReturn(true);
        when(telemetry.progressOf(session.id()))
                .thenReturn(new SessionProgress(new BigDecimal("0.464"), NOW.minus(Duration.ofSeconds(100))));
        when(estimator.forEnergy(any(), any())).thenReturn(Optional.of(new Money(70, "XAF", 0)));

        assertThat(closer.closeIfStalled(session)).isTrue();

        ArgumentCaptor<ChargingSession> closed = ArgumentCaptor.forClass(ChargingSession.class);
        verify(settlement).settle(closed.capture());
        assertThat(closed.getValue().state()).isEqualTo(SessionState.ABANDONED);
        assertThat(closed.getValue().failureCode()).isEqualTo("CHARGER_OFFLINE");
        assertThat(closed.getValue().stopReason()).isEqualTo("PowerLoss");
        assertThat(closed.getValue().energyKwh()).isEqualByComparingTo("0.464");
        assertThat(closed.getValue().costMinor()).isEqualTo(70);
    }
}
