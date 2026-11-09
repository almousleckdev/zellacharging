package com.evcharge.customer.charging.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record Metering(
        BigDecimal totalKwh,
        BigDecimal powerKw,
        BigDecimal stateOfChargePercent,
        Long durationSeconds,
        BigDecimal voltage,
        BigDecimal currentAmpere,
        Instant sampledAt) {

    private static final BigDecimal FULL = BigDecimal.valueOf(100);

    public Metering {
        boolean real = stateOfChargePercent != null && stateOfChargePercent.signum() > 0
                && stateOfChargePercent.compareTo(FULL) <= 0;
        stateOfChargePercent = real ? stateOfChargePercent : null;
    }
}
