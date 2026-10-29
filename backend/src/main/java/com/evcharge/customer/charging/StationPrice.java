package com.evcharge.customer.charging;

import java.time.Instant;

public record StationPrice(String stationId, long pricePerKwhMinor, long sessions, Instant lastUsedAt) {
}
