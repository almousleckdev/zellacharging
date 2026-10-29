package com.evcharge.customer.charging;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyActivity(LocalDate day, long sessions, long completed, long failed, BigDecimal energyKwh,
                            long revenueMinor, long serviceFeeMinor, long vatMinor) {
}
