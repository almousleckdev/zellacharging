package com.evcharge.customer.wallet;

import java.time.LocalDate;

public record DailyMoney(LocalDate day, long topupsMinor, long topupCount, long withdrawalsMinor, long capturedMinor) {
}
