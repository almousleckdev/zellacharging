package com.evcharge.customer.account;

import java.time.Instant;
import java.util.UUID;

public record Complimentary(UUID customerId, String firstName, String lastName, String reason, UUID grantedBy,
                            Instant grantedAt, Instant expiresAt) {
}
