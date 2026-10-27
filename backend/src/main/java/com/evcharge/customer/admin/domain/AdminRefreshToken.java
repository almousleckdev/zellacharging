package com.evcharge.customer.admin.domain;

import java.time.Instant;
import java.util.UUID;

public record AdminRefreshToken(UUID id, UUID adminId, UUID familyId, String tokenHash, Instant expiresAt, Instant usedAt, Instant revokedAt) {

    public boolean isConsumed() {
        return usedAt != null || revokedAt != null;
    }

    public boolean isExpiredAt(Instant now) {
        return !expiresAt.isAfter(now);
    }
}
