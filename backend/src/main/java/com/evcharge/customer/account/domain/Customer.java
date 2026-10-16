package com.evcharge.customer.account.domain;

import java.time.Instant;
import java.util.UUID;

public record Customer(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String passwordHash,
        CustomerStatus status,
        int failedLoginAttempts,
        Instant lockedUntil,
        Instant createdAt) {

    public boolean isLockedAt(Instant now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    public boolean canAuthenticate() {
        return status == CustomerStatus.ACTIVE;
    }
}
