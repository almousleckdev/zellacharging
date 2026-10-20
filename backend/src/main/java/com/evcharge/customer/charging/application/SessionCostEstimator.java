package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.Metering;
import com.evcharge.customer.shared.Money;
import com.evcharge.customer.wallet.WalletFunding;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class SessionCostEstimator {

    private final WalletFunding funding;
    private final SessionSettings settings;

    public SessionCostEstimator(WalletFunding funding, SessionSettings settings) {
        this.funding = funding;
        this.settings = settings;
    }

    public Optional<Money> estimate(ChargingSession session, Optional<Metering> metering) {
        if (session.costMinor() != null) {
            return Optional.empty();
        }
        return metering.map(Metering::totalKwh).flatMap(kwh -> priced(session, kwh));
    }

    public Optional<Money> finalCost(ChargingSession session) {
        if (session.costMinor() != null || session.energyKwh() == null) {
            return Optional.empty();
        }
        return priced(session, session.energyKwh());
    }

    private Optional<Money> priced(ChargingSession session, BigDecimal kwh) {
        if (settings.pricePerKwhMinor() == null) {
            return Optional.empty();
        }
        Money reserved = funding.reserved(session.id());
        return Optional.of(Money.forEnergy(kwh, settings.pricePerKwhMinor(), reserved.currency(),
                reserved.decimals()));
    }
}
