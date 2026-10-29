package com.evcharge.customer.charging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SessionDetail(SessionRecord record, String idTag, Integer transactionId, String stopReason,
                            Long pricePerKwhMinor, BigDecimal lastVoltage, BigDecimal lastPowerKw,
                            BigDecimal lastEnergyKwh, Instant progressAt, Instant updatedAt, Long reservedMinor,
                            Long capturedMinor, String holdState, Long serviceFeeMinor,
                            Long vatMinor) {

    public UUID id() {
        return record.id();
    }
}
