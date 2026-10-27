package com.evcharge.customer.charging.web;

import com.evcharge.customer.charging.domain.Metering;

import java.math.BigDecimal;
import java.time.Instant;

record MeteringResponse(BigDecimal totalKwh, BigDecimal powerKw, BigDecimal stateOfChargePercent,
                        Long durationSeconds, BigDecimal voltage, BigDecimal currentAmpere, Instant sampledAt) {

    static MeteringResponse from(Metering metering) {
        return metering == null ? null : new MeteringResponse(metering.totalKwh(), metering.powerKw(),
                metering.stateOfChargePercent(), metering.durationSeconds(), metering.voltage(),
                metering.currentAmpere(), metering.sampledAt());
    }
}
