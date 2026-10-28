package com.evcharge.customer.charging;

import java.util.List;

public record CommissionPage(CommissionTotals totals, List<CommissionRecord> items) {
}
