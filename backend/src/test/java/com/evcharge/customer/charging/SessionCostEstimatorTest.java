package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.FeeCalculator;
import com.evcharge.customer.charging.application.FeeSettings;
import com.evcharge.customer.charging.application.SessionCostEstimator;
import com.evcharge.customer.charging.application.SessionSettings;
import com.evcharge.customer.charging.application.SessionTariff;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.Metering;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.Money;
import com.evcharge.customer.wallet.WalletFunding;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SessionCostEstimatorTest {

    private static final Instant NOW = Instant.parse("2026-10-16T12:00:00Z");

    private final WalletFunding funding = mock(WalletFunding.class);
    private final SessionRepository sessions = mock(SessionRepository.class);

    private SessionCostEstimator estimator(Long price) {
        when(funding.reserved(any())).thenReturn(new Money(5000, "XAF", 0));
        SessionSettings settings = new SessionSettings(Duration.ofSeconds(60), Duration.ofSeconds(15),
                Duration.ofSeconds(30), price, 95);
        return new SessionCostEstimator(funding, new SessionTariff(sessions, settings),
                new FeeCalculator(new FeeSettings(null, null)));
    }

    private ChargingSession session(Long costMinor) {
        return new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1, "Z-X", SessionState.ACTIVE, null,
                7, NOW, null, null, costMinor, costMinor == null ? null : "XAF", costMinor == null ? null : 0, null,
                NOW, NOW);
    }

    private Optional<Metering> reading(String kwh) {
        return Optional.of(new Metering(new BigDecimal(kwh), null, null, 10L, null, null, NOW));
    }

    @Test
    void estimatesEnergyTimesPriceInTheReservedCurrency() {
        Optional<Money> estimate = estimator(150L).estimate(session(null), reading("2.5"));
        assertThat(estimate).contains(new Money(375, "XAF", 0));
    }

    @Test
    void estimatesNothingWithoutAPrice() {
        assertThat(estimator(null).estimate(session(null), reading("2.5"))).isEmpty();
    }

    @Test
    void estimatesNothingWithoutAReading() {
        assertThat(estimator(150L).estimate(session(null), Optional.empty())).isEmpty();
    }

    @Test
    void estimatesNothingOnceTheFinalCostIsKnown() {
        assertThat(estimator(150L).estimate(session(400L), reading("2.5"))).isEmpty();
    }

    @Test
    void finalCostIsEnergyTimesPriceWhenThePlatformSentNone() {
        ChargingSession done = new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1, "Z-X",
                SessionState.COMPLETED, null, 7, NOW, NOW, new BigDecimal("2.5"), null, null, null, null, NOW, NOW);

        assertThat(estimator(150L).finalCost(done)).contains(new Money(375, "XAF", 0));
    }

    @Test
    void finalCostKeepsThePlatformCost() {
        assertThat(estimator(150L).finalCost(session(400L))).isEmpty();
    }

    @Test
    void thePriceTheChargerPlatformSentWinsOverTheConfiguredOne() {
        SessionCostEstimator estimator = estimator(150L);
        ChargingSession live = session(null);
        when(sessions.priceOf(live.id())).thenReturn(Optional.of(350L));

        assertThat(estimator.estimate(live, reading("2"))).contains(new Money(700, "XAF", 0));
    }
}
