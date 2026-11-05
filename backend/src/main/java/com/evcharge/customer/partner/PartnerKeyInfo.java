package com.evcharge.customer.partner;

import java.time.Instant;
import java.util.UUID;

public record PartnerKeyInfo(UUID id, String partner, String country, Instant createdAt, Instant expiresAt,
                             Instant revokedAt, Instant lastUsedAt) {
}
