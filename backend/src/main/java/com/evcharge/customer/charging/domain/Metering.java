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
}
