package com.evcharge.customer.admin.application;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SeriesPoint(LocalDate day, long topupsMinor, long topupCount, long withdrawalsMinor, long capturedMinor,
                          long sessions, long completed, long failed, BigDecimal energyKwh, long revenueMinor,
                          long serviceFeeMinor, long vatMinor) {
}
