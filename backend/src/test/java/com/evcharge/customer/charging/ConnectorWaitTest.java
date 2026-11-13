package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.ConnectorWait;
import com.evcharge.customer.charging.application.FeeCalculator;
import com.evcharge.customer.charging.application.SessionCostEstimator;
import com.evcharge.customer.charging.application.SessionSettings;
import com.evcharge.customer.charging.application.port.SessionTelemetry;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.FeeBreakdown;
import com.evcharge.customer.charging.domain.LastReading;
import com.evcharge.customer.charging.domain.SessionProgress;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.Money;
import com.evcharge.customer.wallet.WalletFunding;
import org.junit.jupiter.api.Test;

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
import static org.mockito.Mockito.when;

class ConnectorWaitTest {

    private static final Instant NOW = Instant.parse("2026-11-13T12:00:00Z");

    private final SessionTelemetry telemetry = mock(SessionTelemetry.class);
    private final SessionCostEstimator estimator = mock(SessionCostEstimator.class);
    private final FeeCalculator fees = mock(FeeCalculator.class);
    private final WalletFunding funding = mock(WalletFunding.class);
    private final SessionSettings settings = new SessionSettings(Duration.ofMinutes(5), Duration.ofSeconds(15),
            Duration.ofSeconds(30), 100L, 100, Duration.ofSeconds(45), Duration.ofSeconds(5),
            Duration.ofSeconds(10));
    private final ConnectorWait wait = new ConnectorWait(telemetry, settings, estimator, fees, funding,
            Clock.fixed(NOW, ZoneOffset.UTC));

    private ChargingSession session(SessionState state, Instant createdAt) {
        return new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1, "Z-X", state, null, 7,
                createdAt, null, null, null, "XAF", 0, null, createdAt, createdAt);
    }

    private void charging(ChargingSession session, String power, String start, String now, String kwh,
                          long budgetMinor, long perKwhWithFees) {
        when(telemetry.lastReadingOf(session.id())).thenReturn(new LastReading(new BigDecimal(start),
                new BigDecimal(now), null, new BigDecimal(power)));
        when(telemetry.progressOf(session.id())).thenReturn(new SessionProgress(new BigDecimal(kwh), NOW));
        when(funding.reserved(session.id())).thenReturn(new Money(budgetMinor, "XAF", 0));
        when(estimator.forEnergy(any(), any())).thenReturn(Optional.of(new Money(perKwhWithFees, "XAF", 0)));
        when(fees.of(perKwhWithFees)).thenReturn(new FeeBreakdown(perKwhWithFees, 0, 0));
    }

    @Test
    void aChargerWaitingForTheCableFreesWhenTheWaitRunsOut() {
        ChargingSession waiting = session(SessionState.START_ACCEPTED, NOW.minusSeconds(120));

        assertThat(wait.minutesLeft(waiting)).contains(3);
    }

    @Test
    void aChargeLimitedByTheBatteryFreesWhenTheBatteryIsFull() {
        ChargingSession active = session(SessionState.ACTIVE, NOW.minusSeconds(600));
        charging(active, "30", "20", "50", "10.000", 1000000, 100);

        assertThat(wait.minutesLeft(active)).contains(34);
    }

    @Test
    void aChargeLimitedByThePaidBudgetFreesWhenTheBudgetIsSpent() {
        ChargingSession active = session(SessionState.ACTIVE, NOW.minusSeconds(600));
        charging(active, "30", "20", "30", "10.000", 1500, 100);

        assertThat(wait.minutesLeft(active)).contains(10);
    }

    @Test
    void withoutAMeasuredPowerNoTimeIsPromised() {
        ChargingSession active = session(SessionState.ACTIVE, NOW.minusSeconds(600));
        when(telemetry.lastReadingOf(active.id())).thenReturn(LastReading.empty());
        when(telemetry.progressOf(active.id())).thenReturn(SessionProgress.none());

        assertThat(wait.minutesLeft(active)).isEmpty();
    }
}
