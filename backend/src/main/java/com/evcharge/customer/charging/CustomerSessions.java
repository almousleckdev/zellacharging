package com.evcharge.customer.charging;

import java.math.BigDecimal;
import java.time.Instant;

public record CustomerSessions(long completed, long live, BigDecimal energyKwh, long spentMinor, Instant lastSessionAt) {
}
