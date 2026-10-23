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
    private final SessionTariff tariff;
    private final FeeCalculator fees;

    public SessionCostEstimator(WalletFunding funding, SessionTariff tariff, FeeCalculator fees) {
        this.funding = funding;
        this.tariff = tariff;
        this.fees = fees;
    }

    public Optional<Money> estimate(ChargingSession session, Optional<Metering> metering) {
        if (session.costMinor() != null) {
            return Optional.empty();
        }
        return metering.map(Metering::totalKwh).flatMap(kwh -> priced(session, kwh))
                .map(electric -> new Money(fees.of(electric.minor()).totalMinor(), electric.currency(),
                        electric.decimals()));
    }

    public Optional<Money> finalCost(ChargingSession session) {
        if (session.costMinor() != null || session.energyKwh() == null) {
            return Optional.empty();
        }
        return priced(session, session.energyKwh());
    }

    private Optional<Money> priced(ChargingSession session, BigDecimal kwh) {
        return tariff.pricePerKwhMinor(session.id()).map(price -> {
            Money reserved = funding.reserved(session.id());
            return Money.forEnergy(kwh, price, reserved.currency(), reserved.decimals());
        });
    }
}
