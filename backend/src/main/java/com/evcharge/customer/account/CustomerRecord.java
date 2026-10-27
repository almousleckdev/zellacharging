package com.evcharge.customer.account;

import java.time.Instant;
import java.util.UUID;

public record CustomerRecord(UUID id, String firstName, String lastName, String email, String phone, String status,
                             boolean emailVerified, boolean phoneVerified, Instant createdAt) {
}
