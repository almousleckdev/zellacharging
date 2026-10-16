package com.evcharge.customer.wallet.web;

import com.evcharge.customer.wallet.domain.LedgerEntry;

import java.time.Instant;
import java.util.UUID;

record LedgerResponse(UUID id, String type, long amountMinor, UUID sessionId, Instant createdAt) {

    static LedgerResponse from(LedgerEntry entry) {
        return new LedgerResponse(entry.id(), entry.type().name(), entry.amountMinor(), entry.sessionId(),
                entry.createdAt());
    }
}
