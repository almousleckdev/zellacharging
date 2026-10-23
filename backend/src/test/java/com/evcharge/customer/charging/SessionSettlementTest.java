package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.FeeCalculator;
import com.evcharge.customer.charging.application.FeeSettings;
import com.evcharge.customer.charging.application.SessionSettlement;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.wallet.WalletFunding;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SessionSettlementTest {

    private static final Instant NOW = Instant.parse("2026-10-16T12:00:00Z");

    private final WalletFunding funding = mock(WalletFunding.class);

    private ChargingSession completed(Long costMinor) {
        return new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1, "Z-X", SessionState.COMPLETED,
                null, 7, NOW, NOW, null, costMinor, "XAF", 0, null, NOW, NOW);
    }

    @Test
    void chargesTheElectricFeePlusVatAndServiceFee() {
        SessionSettlement settlement = new SessionSettlement(funding,
                new FeeCalculator(new FeeSettings(new BigDecimal("18"), new BigDecimal("5"))));
        ChargingSession session = completed(1000L);

        settlement.settle(session);

        verify(funding).settle(session.id(), 1230L, "XAF");
    }

    @Test
    void settlesWithoutACostWhenNoneIsKnown() {
        SessionSettlement settlement = new SessionSettlement(funding, new FeeCalculator(new FeeSettings(null, null)));
        ChargingSession session = completed(null);

        settlement.settle(session);

        verify(funding).settle(session.id(), null, "XAF");
    }
}
