package com.evcharge.customer.charging;

import java.time.Instant;
import java.util.UUID;

public record CommissionRecord(UUID sessionId, String customerName, String stationId, long electricMinor,
                               long serviceFeeMinor, long vatMinor, long totalMinor, String currency, int decimals,
                               Instant at) {
}
