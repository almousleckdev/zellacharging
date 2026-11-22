package com.evcharge.customer.charging;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.RemoteSession;
import com.evcharge.customer.charging.domain.SessionState;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StopReasonTest {

    private static final Instant NOW = Instant.parse("2026-11-22T21:00:30Z");

    private ChargingSession charging() {
        return new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "DY1", 1, "Z-X", SessionState.ACTIVE, null,
                961, NOW.minusSeconds(3600), null, null, null, null, null, null, NOW.minusSeconds(3600), NOW);
    }

    private RemoteSession completed() {
        return new RemoteSession(961, "DY1", 1, "COMPLETED", NOW.minusSeconds(3600), NOW, new BigDecimal("3.5"),
                1750L, "XOF", 0, "Remote", null, "Z-X", null, null);
    }

    @Test
    void aReasonGivenWhenZellaStopsTheChargeSurvivesTheChargersReport() {
        ChargingSession stopping = charging().withStopReason("FREE_WINDOW_ENDED")
                .withState(SessionState.STOPPING, null, NOW);

        ChargingSession ended = stopping.withRemote(completed(), SessionState.COMPLETED, NOW);

        assertThat(ended.stopReason()).isEqualTo("FREE_WINDOW_ENDED");
        assertThat(ended.energyKwh()).isEqualByComparingTo("3.5");
    }

    @Test
    void theChargersReasonIsKeptWhenZellaGaveNone() {
        ChargingSession stopping = charging().withState(SessionState.STOPPING, null, NOW);

        assertThat(stopping.withRemote(completed(), SessionState.COMPLETED, NOW).stopReason()).isEqualTo("Remote");
        assertThat(charging().withRemote(completed(), SessionState.COMPLETED, NOW).stopReason()).isEqualTo("Remote");
    }
}
