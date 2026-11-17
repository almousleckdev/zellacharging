package com.evcharge.customer.charging;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.RemoteSession;
import com.evcharge.customer.charging.domain.SessionState;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ChargerClockTest {

    private static final Instant NOW = Instant.parse("2026-11-17T18:40:00Z");

    private ChargingSession waiting() {
        return new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "DY1", 2, "Z-X",
                SessionState.START_ACCEPTED, null, null, null, null, null, null, null, null, null,
                NOW.minusSeconds(5), NOW.minusSeconds(5));
    }

    private RemoteSession reportedAt(Instant startedAt) {
        return new RemoteSession(959, "DY1", 2, "ACTIVE", startedAt, null, BigDecimal.ZERO, null, null, null,
                null, null, "Z-X", null, null);
    }

    @Test
    void aChargerClockStuckInThePastDoesNotBecomeTheSessionStartTime() {
        ChargingSession active = waiting().withRemote(reportedAt(Instant.parse("2025-03-06T16:06:27Z")),
                SessionState.ACTIVE, NOW);

        assertThat(active.startedAt()).isEqualTo(NOW);
    }

    @Test
    void aChargerClockCloseToOursIsKept() {
        Instant reported = NOW.minusSeconds(3);

        assertThat(waiting().withRemote(reportedAt(reported), SessionState.ACTIVE, NOW).startedAt())
                .isEqualTo(reported);
    }
}
