package com.evcharge.customer.charging;

import java.time.LocalDate;

public record UsageSummary(LocalDate day, UsageTotals today, UsageTotals yesterday, UsageTotals week,
                           UsageTotals month) {
}
