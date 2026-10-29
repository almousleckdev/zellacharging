package com.evcharge.customer.wallet;

import com.evcharge.customer.shared.LogFilter;

import java.util.UUID;

public interface LedgerLog {

    LedgerPage search(UUID customerId, LogFilter filter, int limit, int offset);
}
