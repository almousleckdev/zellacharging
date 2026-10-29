package com.evcharge.customer.charging;

import com.evcharge.customer.shared.LogFilter;
import com.evcharge.customer.shared.Sort;

public record SessionLogQuery(String state, String text, String station, LogFilter filter, Sort sort, int limit) {
}
