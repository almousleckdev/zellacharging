package com.evcharge.customer.wallet.domain;

import java.time.Instant;
import java.util.UUID;

public record LedgerEntry(
        UUID id,
        UUID customerId,
        LedgerType type,
        long amountMinor,
        UUID sessionId,
        UUID topupId,
        String idempotencyKey,
        Instant createdAt) {
}
