package com.evcharge.customer.charging;

import com.evcharge.customer.shared.LogFilter;
import com.evcharge.customer.shared.Sort;

public record SessionLogQuery(String state, String text, LogFilter filter, Sort sort, int limit) {
}
