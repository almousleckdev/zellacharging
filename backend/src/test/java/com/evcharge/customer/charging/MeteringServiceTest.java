package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.MeteringService;
import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.Metering;
import com.evcharge.customer.charging.domain.SessionState;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MeteringServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-16T12:00:00Z");

    private final ChargingGateway gateway = mock(ChargingGateway.class);
    private final SessionRepository sessions = mock(SessionRepository.class);
    private final MeteringService service = new MeteringService(gateway, sessions);

    private ChargingSession session(SessionState state) {
        return new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1, "Z-X", state, null, 7, NOW,
                null, null, null, null, null, null, NOW, NOW);
    }

    private Optional<Metering> reading(String level) {
        BigDecimal soc = level == null ? null : new BigDecimal(level);
        return Optional.of(new Metering(BigDecimal.ONE, null, soc, 10L, null, null, NOW));
    }

    @Test
    void remembersTheBatteryLevelOfALiveReading() {
        ChargingSession live = session(SessionState.ACTIVE);
        when(gateway.metering(7)).thenReturn(reading("62.5"));

        service.of(live);

        verify(sessions).recordBattery(live.id(), new BigDecimal("62.5"));
    }

    @Test
    void doesNotRememberAReadingWithoutALevel() {
        when(gateway.metering(7)).thenReturn(reading(null));

        service.of(session(SessionState.ACTIVE));

        verify(sessions, never()).recordBattery(any(), any());
    }

    @Test
    void doesNotAskTheChargerAboutAFinishedSession() {
        assertThat(service.of(session(SessionState.COMPLETED))).isEmpty();

        verify(gateway, never()).metering(anyInt());
    }

    @Test
    void fallsBackToTheRememberedLevelWhenThereIsNoReading() {
        ChargingSession done = session(SessionState.COMPLETED);
        when(sessions.batteryOf(done.id())).thenReturn(Optional.of(new BigDecimal("100")));

        assertThat(service.batteryOf(done, Optional.empty())).contains(new BigDecimal("100"));
    }

    @Test
    void prefersTheLiveLevelOverTheRememberedOne() {
        ChargingSession live = session(SessionState.ACTIVE);

        assertThat(service.batteryOf(live, reading("71"))).contains(new BigDecimal("71"));
    }
}
