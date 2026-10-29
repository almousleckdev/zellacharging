package com.evcharge.customer.wallet;

import java.util.List;

public record LedgerPage(long total, String currency, int decimals, List<LedgerRecord> items) {
}
