package com.evcharge.customer.charging.web;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.FeeBreakdown;
import com.evcharge.customer.charging.domain.LastReading;
import com.evcharge.customer.charging.domain.Metering;
import com.evcharge.customer.shared.Money;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

record SessionResponse(
        UUID id,
        String stationId,
        int connectorId,
        String state,
        String failureCode,
        Instant startedAt,
        Instant stoppedAt,
        BigDecimal energyKwh,
        Long costMinor,
        String currency,
        Integer currencyDecimals,
        String stopReason,
        Instant createdAt,
        MeteringResponse metering,
        BigDecimal batteryPercent,
        BigDecimal lastVoltage,
        BigDecimal lastPowerKw,
        FeesResponse fees,
        EstimatedCostResponse estimatedCost) {

    static SessionResponse from(ChargingSession s, Metering metering, LastReading last, Optional<Money> estimate,
                                Optional<FeeBreakdown> fees) {
        return new SessionResponse(s.id(), s.stationId(), s.connectorId(), s.state().name(), s.failureCode(),
                s.startedAt(), s.stoppedAt(), s.energyKwh(), s.costMinor(), s.currency(), s.currencyDecimals(),
                s.stopReason(), s.createdAt(), MeteringResponse.from(metering),
                last.batteryPercent(), last.voltage(), last.powerKw(), FeesResponse.from(s, fees),
                EstimatedCostResponse.from(estimate));
    }
}
