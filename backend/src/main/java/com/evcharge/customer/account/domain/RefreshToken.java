package com.evcharge.customer.account.domain;

import java.time.Instant;
import java.util.UUID;

public record RefreshToken(
        UUID id,
        UUID customerId,
        UUID familyId,
        String tokenHash,
        Instant expiresAt,
        Instant usedAt,
        Instant revokedAt) {

    public boolean isExpiredAt(Instant now) {
        return !expiresAt.isAfter(now);
    }

    public boolean isConsumed() {
        return usedAt != null || revokedAt != null;
    }
}
