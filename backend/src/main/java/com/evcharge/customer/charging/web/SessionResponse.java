package com.evcharge.customer.charging.web;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.Metering;

import java.math.BigDecimal;
import java.time.Instant;
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
        MeteringResponse metering) {

    record MeteringResponse(BigDecimal totalKwh, BigDecimal powerKw, BigDecimal stateOfChargePercent,
                            Long durationSeconds, Instant sampledAt) {

        static MeteringResponse from(Metering metering) {
            return metering == null ? null : new MeteringResponse(metering.totalKwh(), metering.powerKw(),
                    metering.stateOfChargePercent(), metering.durationSeconds(), metering.sampledAt());
        }
    }

    static SessionResponse from(ChargingSession s, Metering metering) {
        return new SessionResponse(s.id(), s.stationId(), s.connectorId(), s.state().name(), s.failureCode(),
                s.startedAt(), s.stoppedAt(), s.energyKwh(), s.costMinor(), s.currency(), s.currencyDecimals(),
                s.stopReason(), s.createdAt(), MeteringResponse.from(metering));
    }
}
