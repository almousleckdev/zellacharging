package com.evcharge.customer.charging.domain;

import java.math.BigDecimal;

public record LastReading(BigDecimal startBatteryPercent, BigDecimal batteryPercent, BigDecimal voltage,
                          BigDecimal powerKw) {

    public static LastReading empty() {
        return new LastReading(null, null, null, null);
    }

    public static LastReading of(Metering metering) {
        return new LastReading(null, metering.stateOfChargePercent(), metering.voltage(), metering.powerKw());
    }

    public LastReading orElse(LastReading fallback) {
        return new LastReading(
                fallback.startBatteryPercent,
                batteryPercent != null ? batteryPercent : fallback.batteryPercent,
                voltage != null ? voltage : fallback.voltage,
                powerKw != null ? powerKw : fallback.powerKw);
    }
}
