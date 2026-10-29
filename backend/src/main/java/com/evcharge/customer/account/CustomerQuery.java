package com.evcharge.customer.account;

import com.evcharge.customer.shared.Sort;

public record CustomerQuery(String text, String status, Sort sort, int limit, int offset) {
}
