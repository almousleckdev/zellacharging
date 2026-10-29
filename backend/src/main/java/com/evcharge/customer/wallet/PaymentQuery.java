package com.evcharge.customer.wallet;

import com.evcharge.customer.shared.LogFilter;
import com.evcharge.customer.shared.Sort;

public record PaymentQuery(String kind, String state, String text, LogFilter filter, Sort sort, int limit) {
}
