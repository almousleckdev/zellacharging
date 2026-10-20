package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.domain.Metering;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
record OcppMeteringPayload(
        BigDecimal totalKwh,
        BigDecimal currentPowerKw,
        BigDecimal stateOfChargePercent,
        Long durationSeconds,
        BigDecimal voltage,
        BigDecimal currentAmpere,
        Instant sampledAt) {

    Metering toDomain() {
        return new Metering(totalKwh, currentPowerKw, stateOfChargePercent, durationSeconds, voltage,
                currentAmpere, sampledAt);
    }
}
