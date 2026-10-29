package com.evcharge.customer.charging;

import java.util.List;

public record CommissionPage(CommissionTotals totals, long customers, List<CommissionDigest> items) {
}
