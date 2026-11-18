package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.CountryFreeUsage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

record FreeUsageResponse(UUID customerId, String country, String customerName, long sessions, BigDecimal energyKwh,
                         long valueMinor, String currency, Integer decimals, Instant lastChargeAt) {

    static FreeUsageResponse from(CountryFreeUsage entry) {
        var row = entry.usage();
        return new FreeUsageResponse(row.customerId(), entry.country(), row.customerName(), row.sessions(),
                row.energyKwh(), row.valueMinor(), row.currency(), row.decimals(), row.lastChargeAt());
    }
}
