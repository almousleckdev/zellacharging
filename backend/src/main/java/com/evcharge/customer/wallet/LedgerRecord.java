package com.evcharge.customer.wallet;

import java.time.Instant;
import java.util.UUID;

public record LedgerRecord(UUID id, String type, long amountMinor, UUID sessionId, UUID topupId, Instant createdAt) {
}
