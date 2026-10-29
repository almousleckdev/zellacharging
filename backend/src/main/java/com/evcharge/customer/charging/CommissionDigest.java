package com.evcharge.customer.charging;

import java.time.Instant;
import java.util.UUID;

public record CommissionDigest(UUID customerId, String customerName, long sessions, long electricMinor,
                               long serviceFeeMinor, long vatMinor, String currency, int decimals, Instant lastAt) {

    public long totalMinor() {
        return electricMinor + serviceFeeMinor + vatMinor;
    }
}
