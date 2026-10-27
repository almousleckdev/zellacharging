package com.evcharge.customer.charging;

import java.math.BigDecimal;

public record SessionFigures(long live, long completed, long startedLast24Hours, BigDecimal energyKwh,
                             long revenueMinor) {
}
