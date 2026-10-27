package com.evcharge.customer.charging.domain;

import java.math.BigDecimal;

public record SessionQuote(
        long amountMinor,
        String currency,
        int decimals,
        long availableMinor,
        long shortfallMinor,
        long minimumMinor,
        long maximumMinor,
        BigDecimal vatPercent,
        BigDecimal serviceFeePercent,
        Long pricePerKwhMinor,
        BigDecimal estimatedEnergyKwh) {
}
