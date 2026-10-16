package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.FundsGuard;
import com.evcharge.customer.charging.application.MeteringService;
import com.evcharge.customer.charging.application.SessionSettings;
import com.evcharge.customer.charging.application.StopCommander;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.CommandOutcome;
import com.evcharge.customer.charging.domain.Metering;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.Money;
import com.evcharge.customer.wallet.WalletFunding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FundsGuardTest {

    private static final Instant NOW = Instant.parse("2026-10-16T12:00:00Z");

    private final WalletFunding funding = mock(WalletFunding.class);
    private final MeteringService metering = mock(MeteringService.class);
    private final StopCommander stopper = mock(StopCommander.class);
    private final SessionSettings settings = new SessionSettings(Duration.ofSeconds(60), Duration.ofSeconds(15),
            Duration.ofSeconds(30), 150L, 95);
    private FundsGuard guard;

    @BeforeEach
    void setUp() {
        guard = new FundsGuard(funding, metering, stopper, settings, Clock.fixed(NOW, ZoneOffset.UTC));
        when(funding.reserved(any())).thenReturn(new Money(5000, "XOF", 0));
        when(stopper.issue(any())).thenReturn(CommandOutcome.success());
    }

    private ChargingSession session(SessionState state, Instant updatedAt) {
        return new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1, "Z-X", state, null, 7, NOW,
                null, null, null, null, null, null, NOW, updatedAt);
    }

    private void energy(String kwh) {
        when(metering.of(any())).thenReturn(Optional.of(new Metering(new BigDecimal(kwh), null, null, 10L, NOW)));
    }

    @Test
    void stopsWhenEstimatedCostReachesTheThreshold() {
        energy("31.7");
        guard.enforce(session(SessionState.ACTIVE, NOW));
        verify(stopper).issue(any());
    }

    @Test
    void keepsChargingWhileBelowTheThreshold() {
        energy("31.0");
        guard.enforce(session(SessionState.ACTIVE, NOW));
        verify(stopper, never()).issue(any());
    }

    @Test
    void doesNothingWithoutAPriceOrMetering() {
        when(metering.of(any())).thenReturn(Optional.empty());
        guard.enforce(session(SessionState.ACTIVE, NOW));
        verify(stopper, never()).issue(any());
    }

    @Test
    void reissuesAStopThatIsStuckForTooLong() {
        guard.enforce(session(SessionState.STOPPING, NOW.minusSeconds(45)));
        verify(stopper).issue(any());
    }

    @Test
    void leavesAFreshStopAlone() {
        guard.enforce(session(SessionState.STOPPING, NOW.minusSeconds(5)));
        verify(stopper, never()).issue(any());
    }
}
