package com.evcharge.customer.charging;

import com.evcharge.customer.shared.LogFilter;

public record CommissionQuery(String text, LogFilter filter, int limit) {
}
