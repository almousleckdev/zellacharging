package com.evcharge.customer.charging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FreeUsageRow(UUID customerId, String customerName, long sessions, BigDecimal energyKwh,
                           long valueMinor, String currency, Integer decimals, Instant lastChargeAt) {
}
