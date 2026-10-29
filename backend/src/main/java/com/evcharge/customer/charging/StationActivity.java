package com.evcharge.customer.charging;

import java.math.BigDecimal;
import java.time.Instant;

public record StationActivity(String stationId, long sessions, long live, BigDecimal energyKwh, long revenueMinor,
                              Instant lastAt) {
}
