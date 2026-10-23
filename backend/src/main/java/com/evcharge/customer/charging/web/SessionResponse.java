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

    record MeteringResponse(BigDecimal totalKwh, BigDecimal powerKw, BigDecimal stateOfChargePercent,
                            Long durationSeconds, BigDecimal voltage, BigDecimal currentAmpere,
                            Instant sampledAt) {

        static MeteringResponse from(Metering metering) {
            return metering == null ? null : new MeteringResponse(metering.totalKwh(), metering.powerKw(),
                    metering.stateOfChargePercent(), metering.durationSeconds(), metering.voltage(),
                    metering.currentAmpere(), metering.sampledAt());
        }
    }

    record EstimatedCostResponse(long minor, String currency, int decimals) {

        static EstimatedCostResponse from(Optional<Money> estimate) {
            return estimate.map(m -> new EstimatedCostResponse(m.minor(), m.currency(), m.decimals())).orElse(null);
        }
    }

    record FeesResponse(long electricMinor, long vatMinor, long serviceMinor, long totalMinor, String currency,
                        int decimals) {

        static FeesResponse from(ChargingSession s, Optional<FeeBreakdown> fees) {
            return fees.map(f -> new FeesResponse(f.electricMinor(), f.vatMinor(), f.serviceMinor(), f.totalMinor(),
                    s.currency(), s.currencyDecimals() == null ? 0 : s.currencyDecimals())).orElse(null);
        }
    }

    static SessionResponse from(ChargingSession s, Metering metering, LastReading last, Optional<Money> estimate,
                                Optional<FeeBreakdown> fees) {
        return new SessionResponse(s.id(), s.stationId(), s.connectorId(), s.state().name(), s.failureCode(),
                s.startedAt(), s.stoppedAt(), s.energyKwh(), s.costMinor(), s.currency(), s.currencyDecimals(),
                s.stopReason(), s.createdAt(), MeteringResponse.from(metering),
                last.batteryPercent(), last.voltage(), last.powerKw(), FeesResponse.from(s, fees),
                EstimatedCostResponse.from(estimate));
    }
}
