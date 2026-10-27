package com.evcharge.customer.admin.application.port;

import com.evcharge.customer.admin.domain.AdminUser;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AdminRepository {

    Optional<AdminUser> findByEmail(String email);
    Optional<AdminUser> findById(UUID id);
    boolean existsSuperAdmin();
    void create(AdminUser admin, Instant at);
    void recordFailure(UUID id, int attempts, Instant lockedUntil);
    void clearFailures(UUID id);
    void updatePassword(UUID id, String passwordHash);
    void recordLogin(UUID id, Instant at);
    void storePendingSecret(UUID id, String encryptedSecret);
    boolean activateTotp(UUID id, String encryptedSecret, long step);
    boolean advanceTotpStep(UUID id, long step);
}
