package com.evcharge.customer.wallet.domain;

import java.time.Instant;
import java.util.Optional;
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
    public Optional<UUID> withdrawalId() {
        if (type != LedgerType.WITHDRAWAL && type != LedgerType.WITHDRAWAL_REVERSAL) {
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(idempotencyKey.substring(idempotencyKey.indexOf(':') + 1)));
    }
}
