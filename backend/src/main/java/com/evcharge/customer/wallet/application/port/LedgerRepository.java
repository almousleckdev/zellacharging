package com.evcharge.customer.wallet.application.port;

import com.evcharge.customer.wallet.domain.LedgerDiscrepancy;
import com.evcharge.customer.wallet.domain.LedgerEntry;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LedgerRepository {

    boolean append(LedgerEntry entry);

    List<LedgerEntry> recent(UUID customerId, int limit);

    Optional<LedgerEntry> find(UUID customerId, UUID id);

    long balanceAfter(UUID customerId, Instant at);

    boolean hide(UUID customerId, UUID id, Instant at);

    List<LedgerDiscrepancy> discrepancies();
}
