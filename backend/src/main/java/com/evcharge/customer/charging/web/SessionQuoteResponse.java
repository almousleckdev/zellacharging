package com.evcharge.customer.charging.web;

import com.evcharge.customer.charging.domain.SessionQuote;

import java.math.BigDecimal;

record SessionQuoteResponse(
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

    static SessionQuoteResponse from(SessionQuote quote) {
        return new SessionQuoteResponse(quote.amountMinor(), quote.currency(), quote.decimals(),
                quote.availableMinor(), quote.shortfallMinor(), quote.minimumMinor(), quote.maximumMinor(),
                quote.vatPercent(), quote.serviceFeePercent(), quote.pricePerKwhMinor(),
                quote.estimatedEnergyKwh());
    }
}
