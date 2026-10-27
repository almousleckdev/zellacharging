package com.evcharge.customer.charging.web;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.FeeBreakdown;

import java.util.Optional;

record FeesResponse(long electricMinor, long vatMinor, long serviceMinor, long totalMinor, String currency,
                    int decimals) {

    static FeesResponse from(ChargingSession s, Optional<FeeBreakdown> fees) {
        return fees.map(f -> new FeesResponse(f.electricMinor(), f.vatMinor(), f.serviceMinor(), f.totalMinor(),
                s.currency(), s.currencyDecimals() == null ? 0 : s.currencyDecimals())).orElse(null);
    }
}
