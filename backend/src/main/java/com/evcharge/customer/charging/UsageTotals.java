package com.evcharge.customer.charging;

import java.math.BigDecimal;

public record UsageTotals(long sessions, BigDecimal energyKwh, long collectedMinor, BigDecimal staffEnergyKwh) {
}
