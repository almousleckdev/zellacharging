package com.evcharge.customer.charging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SessionRecord(UUID id, UUID customerId, String customerName, String stationId, int connectorId,
                            String state, String failureCode, BigDecimal energyKwh, Long costMinor, String currency,
                            Integer decimals, BigDecimal startBatteryPercent, BigDecimal endBatteryPercent,
                            Instant startedAt, Instant stoppedAt, Instant createdAt, String freeCategory) {
}
