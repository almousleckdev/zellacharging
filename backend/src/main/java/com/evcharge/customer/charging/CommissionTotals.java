package com.evcharge.customer.charging;

import java.math.BigDecimal;

public record CommissionTotals(long sessions, long electricMinor, long serviceFeeMinor, long vatMinor,
                               BigDecimal serviceFeePercent, BigDecimal vatPercent) {
}
