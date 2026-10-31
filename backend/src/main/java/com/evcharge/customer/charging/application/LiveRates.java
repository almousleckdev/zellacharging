package com.evcharge.customer.charging.application;

import com.evcharge.customer.shared.RuntimeSettings;
import com.evcharge.customer.shared.SettingKey;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Primary
@Component
public class LiveRates implements FeeRates, PriceList {

    private final FeeSettings fees;
    private final SessionSettings sessions;
    private final RuntimeSettings runtime;

    LiveRates(FeeSettings fees, SessionSettings sessions, RuntimeSettings runtime) {
        this.fees = fees;
        this.sessions = sessions;
        this.runtime = runtime;
    }

    @Override
    public BigDecimal vatPercent() {
        return runtime.decimal(SettingKey.VAT, fees.vatPercent());
    }

    @Override
    public BigDecimal serviceFeePercent() {
        return runtime.decimal(SettingKey.SERVICE_FEE, fees.serviceFeePercent());
    }

    @Override
    public Long pricePerKwhMinor() {
        return runtime.optionalNumber(SettingKey.PRICE_PER_KWH, sessions.pricePerKwhMinor());
    }
}
