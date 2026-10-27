package com.evcharge.customer.admin.domain;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public record AdminUser(
        UUID id,
        String email,
        String passwordHash,
        AdminRole role,
        AdminStatus status,
        String firstName,
        String lastName,
        String phone,
        String totpSecret,
        String totpPendingSecret,
        Long totpLastStep,
        int failedAttempts,
        Instant lockedUntil,
        Instant lastLoginAt,
        Set<Permission> permissions,
        Set<String> grantedCountries) {

    public static AdminUser create(UUID id, String email, String passwordHash, AdminRole role, AdminStatus status,
                                   Set<Permission> permissions, Set<String> countries) {
        return new AdminUser(id, email, passwordHash, role, status, null, null, null, null, null, null, 0, null,
                null, permissions, countries);
    }

    public AdminUser withName(String first, String last) {
        return new AdminUser(id, email, passwordHash, role, status, first, last, phone, totpSecret,
                totpPendingSecret, totpLastStep, failedAttempts, lockedUntil, lastLoginAt, permissions,
                grantedCountries);
    }

    public AdminUser withTotpSecret(String encryptedSecret) {
        return new AdminUser(id, email, passwordHash, role, status, firstName, lastName, phone, encryptedSecret,
                totpPendingSecret, totpLastStep, failedAttempts, lockedUntil, lastLoginAt, permissions,
                grantedCountries);
    }

    public boolean active() {
        return status == AdminStatus.ACTIVE;
    }

    public boolean profileComplete() {
        return notBlank(firstName) && notBlank(lastName);
    }

    public boolean isLockedAt(Instant now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    public boolean hasTotp() {
        return totpSecret != null;
    }

    public Set<Permission> effectivePermissions() {
        return role == AdminRole.SUPER_ADMIN ? EnumSet.allOf(Permission.class) : permissions;
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
