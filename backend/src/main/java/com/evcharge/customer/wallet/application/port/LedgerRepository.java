package com.evcharge.customer.wallet.application.port;

import com.evcharge.customer.wallet.domain.LedgerEntry;

import java.util.List;
import java.util.UUID;

public interface LedgerRepository {

    boolean append(LedgerEntry entry);

    List<LedgerEntry> recent(UUID customerId, int limit);
}
