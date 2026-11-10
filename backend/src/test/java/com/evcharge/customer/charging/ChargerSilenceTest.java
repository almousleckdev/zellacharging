package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.ChargerSilence;
import com.evcharge.customer.charging.application.StallSettings;
import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.SessionTelemetry;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.Metering;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

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

class ChargerSilenceTest {

    private static final Instant NOW = Instant.parse("2026-11-09T10:00:00Z");

    private final SessionTelemetry telemetry = mock(SessionTelemetry.class);
    private final ChargingGateway gateway = mock(ChargingGateway.class);
    private final ChargerSilence silence = new ChargerSilence(telemetry, gateway,
            new StallSettings(Duration.ofMinutes(5), Duration.ofSeconds(90)), Clock.fixed(NOW, ZoneOffset.UTC));

    private ChargingSession active(Integer transactionId) {
        return new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1, "Z-X", SessionState.ACTIVE,
                null, transactionId, NOW.minusSeconds(600), null, null, null, "XAF", 0, null, NOW.minusSeconds(600), NOW);
    }

    private static Metering sample() {
        return new Metering(new BigDecimal("1.2"), null, null, null, null, null, NOW);
    }

    @Test
    void aChargerThatReportedRecentlyIsNotSilentAndIsNotPolled() {
        ChargingSession session = active(7);
        when(telemetry.readingAtOf(session.id())).thenReturn(Optional.of(NOW.minusSeconds(30)));

        assertThat(silence.confirmed(session)).isFalse();

        verify(gateway, never()).metering(7);
    }

    @Test
    void aChargeThatJustStartedIsNeverJudgedSilentWhileItsFirstReadingIsStillComing() {
        ChargingSession fresh = new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1, "Z-X",
                SessionState.ACTIVE, null, 7, NOW.minusSeconds(40), null, null, null, "XAF", 0, null,
                NOW.minusSeconds(45), NOW);
        when(telemetry.readingAtOf(fresh.id())).thenReturn(Optional.of(NOW.minusSeconds(40)));
        when(gateway.metering(7)).thenReturn(Optional.of(sample()));

        assertThat(silence.confirmed(fresh)).isFalse();

        verify(gateway, never()).metering(7);
    }

    @Test
    void aSessionThatNeverReportedAnythingIsNotJudgedSilent() {
        ChargingSession session = active(7);
        when(telemetry.readingAtOf(session.id())).thenReturn(Optional.empty());

        assertThat(silence.confirmed(session)).isFalse();
    }

    @Test
    void silenceIsConfirmedOnlyWhenAFreshPollBringsNothingNew() {
        ChargingSession session = active(7);
        when(telemetry.readingAtOf(session.id())).thenReturn(Optional.of(NOW.minusSeconds(120)));
        when(gateway.metering(7)).thenReturn(Optional.of(sample()));

        assertThat(silence.confirmed(session)).isTrue();

        verify(telemetry).recordReading(any(), any());
    }

    @Test
    void aFreshSampleFoundByThePollMeansTheChargerIsAlive() {
        ChargingSession session = active(7);
        when(telemetry.readingAtOf(session.id()))
                .thenReturn(Optional.of(NOW.minusSeconds(120)), Optional.of(NOW));
        when(gateway.metering(7)).thenReturn(Optional.of(sample()));

        assertThat(silence.confirmed(session)).isFalse();
    }

    @Test
    void nothingIsClosedWhenThePlatformCannotBeAsked() {
        ChargingSession session = active(7);
        when(telemetry.readingAtOf(session.id())).thenReturn(Optional.of(NOW.minusSeconds(300)));
        when(gateway.metering(7)).thenThrow(new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "CHARGING_UNAVAILABLE", "x"));

        assertThat(silence.confirmed(session)).isFalse();
    }

    @Test
    void aSessionWithoutATransactionCannotBeVerified() {
        assertThat(silence.confirmed(active(null))).isFalse();
    }
}
