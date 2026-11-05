package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.MeteringService;
import com.evcharge.customer.charging.application.SessionSettings;
import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.SessionTelemetry;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.LastReading;
import com.evcharge.customer.charging.domain.Metering;
import com.evcharge.customer.charging.domain.SessionProgress;
import com.evcharge.customer.charging.domain.SessionState;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MeteringServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-16T12:00:00Z");

    private final ChargingGateway gateway = mock(ChargingGateway.class);
    private final SessionTelemetry sessions = mock(SessionTelemetry.class);
    private final MeteringService service = new MeteringService(gateway, sessions,
            new SessionSettings(Duration.ofSeconds(60), Duration.ofSeconds(15), Duration.ofSeconds(30), 150L, 95,
                    Duration.ofSeconds(90), Duration.ofSeconds(5), Duration.ofSeconds(30)),
            Clock.fixed(NOW, ZoneOffset.UTC));

    {
        when(sessions.progressOf(org.mockito.ArgumentMatchers.any())).thenReturn(SessionProgress.none());
    }

    private ChargingSession session(SessionState state) {
        return new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1, "Z-X", state, null, 7, NOW,
                null, null, null, null, null, null, NOW, NOW);
    }

    private Optional<Metering> reading(String level) {
        BigDecimal soc = level == null ? null : new BigDecimal(level);
        return Optional.of(new Metering(BigDecimal.ONE, null, soc, 10L, null, null, NOW));
    }

    @Test
    void remembersTheLiveReading() {
        ChargingSession live = session(SessionState.ACTIVE);
        Optional<Metering> found = reading("62.5");
        when(gateway.metering(7)).thenReturn(found);

        service.of(live);

        verify(sessions).recordReading(live.id(), found.orElseThrow());
    }

    @Test
    void doesNotAskTheChargerAboutAFinishedSession() {
        assertThat(service.of(session(SessionState.COMPLETED))).isEmpty();

        verify(gateway, never()).metering(anyInt());
    }

    @Test
    void fallsBackToTheRememberedReadingWhenThereIsNoLiveOne() {
        ChargingSession done = session(SessionState.COMPLETED);
        when(sessions.lastReadingOf(done.id()))
                .thenReturn(new LastReading(null, new BigDecimal("100"), new BigDecimal("401"), new BigDecimal("22")));

        LastReading last = service.lastReading(done, Optional.empty());

        assertThat(last.batteryPercent()).isEqualByComparingTo("100");
        assertThat(last.voltage()).isEqualByComparingTo("401");
    }

    @Test
    void prefersLiveValuesAndKeepsRememberedOnesForWhatIsMissing() {
        ChargingSession live = session(SessionState.ACTIVE);
        when(sessions.lastReadingOf(live.id()))
                .thenReturn(new LastReading(null, new BigDecimal("50"), new BigDecimal("399"), null));

        LastReading last = service.lastReading(live, reading("71"));

        assertThat(last.batteryPercent()).isEqualByComparingTo("71");
        assertThat(last.voltage()).isEqualByComparingTo("399");
    }

    @Test
    void aRecentReadingIsServedFromMemoryInsteadOfAskingTheChargerAgain() {
        ChargingSession active = session(SessionState.ACTIVE);
        when(gateway.metering(7)).thenReturn(reading("40"));

        service.of(active);
        service.of(active);
        service.of(active);

        verify(gateway, org.mockito.Mockito.times(1)).metering(7);
    }

    @Test
    void usesTheLatestWebhookReadingWhenThePlatformHasNoneToPoll() {
        ChargingSession live = session(SessionState.ACTIVE);
        when(gateway.metering(7)).thenReturn(Optional.empty());
        when(sessions.progressOf(live.id())).thenReturn(new SessionProgress(new BigDecimal("4.2"),
                NOW.minusSeconds(30)));
        when(sessions.lastReadingOf(live.id())).thenReturn(new LastReading(null, new BigDecimal("93"),
                new BigDecimal("400"), new BigDecimal("30")));

        Metering used = service.of(live).orElseThrow();

        assertThat(used.totalKwh()).isEqualByComparingTo("4.2");
        assertThat(used.stateOfChargePercent()).isEqualByComparingTo("93");
        assertThat(used.voltage()).isEqualByComparingTo("400");
        assertThat(used.powerKw()).isEqualByComparingTo("30");
    }

    @Test
    void ignoresAWebhookReadingThatIsTooOldToTrust() {
        ChargingSession live = session(SessionState.ACTIVE);
        when(gateway.metering(7)).thenReturn(Optional.empty());
        when(sessions.progressOf(live.id())).thenReturn(new SessionProgress(new BigDecimal("4.2"),
                NOW.minusSeconds(600)));

        assertThat(service.of(live)).isEmpty();
    }

    @Test
    void staysEmptyWhenNeitherThePlatformNorTheWebhookHasAnyReading() {
        ChargingSession live = session(SessionState.ACTIVE);
        when(gateway.metering(7)).thenReturn(Optional.empty());

        assertThat(service.of(live)).isEmpty();
    }
}
