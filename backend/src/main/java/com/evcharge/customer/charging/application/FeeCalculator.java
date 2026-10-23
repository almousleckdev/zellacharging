package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.domain.FeeBreakdown;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class FeeCalculator {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final FeeSettings settings;

    public FeeCalculator(FeeSettings settings) {
        this.settings = settings;
    }

    public FeeBreakdown of(long electricMinor) {
        return new FeeBreakdown(electricMinor, share(electricMinor, settings.vatPercent()),
                share(electricMinor, settings.serviceFeePercent()));
    }

    private static long share(long amountMinor, BigDecimal percent) {
        return BigDecimal.valueOf(amountMinor).multiply(percent).divide(HUNDRED, 0, RoundingMode.HALF_UP).longValue();
    }
}
