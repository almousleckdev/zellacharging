package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.FeeCalculator;
import com.evcharge.customer.charging.application.FeeSettings;
import com.evcharge.customer.charging.application.SessionCostEstimator;
import com.evcharge.customer.charging.application.SessionReconciler;
import com.evcharge.customer.charging.application.SessionSettings;
import com.evcharge.customer.charging.application.SessionSettlement;
import com.evcharge.customer.charging.application.SessionTariff;
import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.RemoteSession;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.Money;
import com.evcharge.customer.wallet.WalletFunding;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessionReconcilerTest {

    private static final Instant NOW = Instant.parse("2026-10-16T12:00:00Z");

    private final ChargingGateway gateway = mock(ChargingGateway.class);
    private final SessionRepository sessions = mock(SessionRepository.class);
    private final SessionSettlement settlement = mock(SessionSettlement.class);
    private final WalletFunding funding = mock(WalletFunding.class);

    private SessionReconciler reconciler(Long price) {
        when(funding.reserved(any())).thenReturn(new Money(5000, "XAF", 0));
        SessionSettings settings = new SessionSettings(Duration.ofSeconds(60), Duration.ofSeconds(15),
                Duration.ofSeconds(30), price, 95);
        return new SessionReconciler(gateway, sessions, settings, settlement,
                new SessionCostEstimator(funding, new SessionTariff(sessions, settings),
                        new FeeCalculator(new FeeSettings(null, null))),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private ChargingSession stopping() {
        return new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1, "Z-X",
                SessionState.STOPPING, null, 7, NOW.minusSeconds(600), null, null, null, null, null, null,
                NOW.minusSeconds(660), NOW.minusSeconds(30));
    }

    private void platformReports(Long costMinor) {
        platformReports(costMinor, null);
    }

    private void platformReports(Long costMinor, Long platformPrice) {
        RemoteSession done = new RemoteSession(7, "CP-1", 1, "COMPLETED", NOW.minusSeconds(600), NOW,
                new BigDecimal("2.5"), costMinor, costMinor == null ? null : "XAF", costMinor == null ? null : 0,
                "Remote", platformPrice);
        when(gateway.sessionsOf("Z-X")).thenReturn(List.of(done));
    }

    @Test
    void chargesEnergyTimesPriceWhenThePlatformSendsNoCost() {
        platformReports(null);

        ChargingSession result = reconciler(150L).reconcile(stopping());

        assertThat(result.state()).isEqualTo(SessionState.COMPLETED);
        assertThat(result.costMinor()).isEqualTo(375L);
        assertThat(result.currency()).isEqualTo("XAF");
        verify(sessions).update(result);
        verify(settlement).settle(result);
    }

    @Test
    void keepsTheCostTheChargerPlatformSent() {
        platformReports(400L);

        assertThat(reconciler(150L).reconcile(stopping()).costMinor()).isEqualTo(400L);
    }

    @Test
    void leavesTheCostEmptyWhenNobodyCanPriceTheSession() {
        platformReports(null);

        assertThat(reconciler(null).reconcile(stopping()).costMinor()).isNull();
    }

    @Test
    void remembersThePriceThePlatformChargedPerKwh() {
        platformReports(null, 350L);
        ChargingSession live = stopping();

        reconciler(150L).reconcile(live);

        verify(sessions).recordPrice(live.id(), 350L);
    }
}
