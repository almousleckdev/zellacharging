package com.evcharge.customer.account.domain;

import java.time.Instant;
import java.util.UUID;

public record VerificationCode(
        UUID id,
        UUID customerId,
        Channel channel,
        String codeHash,
        Instant expiresAt,
        int attempts,
        Instant consumedAt,
        Instant createdAt) {

    public boolean isUsableAt(Instant now, int maxAttempts) {
        return consumedAt == null && expiresAt.isAfter(now) && attempts < maxAttempts;
    }
}
