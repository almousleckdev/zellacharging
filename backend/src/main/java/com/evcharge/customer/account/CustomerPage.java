package com.evcharge.customer.account;

import java.util.List;

public record CustomerPage(long total, List<CustomerRecord> items) {
}
