package com.evcharge.customer.partner.domain;

import java.time.Instant;
import java.util.UUID;

public record PartnerKey(UUID id, String partner, String country, String keyHash, Instant createdAt,
                         Instant expiresAt, Instant revokedAt, Instant lastUsedAt) {

    public boolean usableAt(Instant now) {
        return revokedAt == null && (expiresAt == null || expiresAt.isAfter(now));
    }
}
