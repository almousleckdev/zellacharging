package com.evcharge.customer.admin.application.port;

import com.evcharge.customer.admin.domain.AdminPage;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.admin.domain.AdminStatus;
import com.evcharge.customer.admin.domain.AdminUser;
import com.evcharge.customer.admin.domain.Permission;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface AdminAccountRepository {
    AdminPage search(String text, AdminStatus status, int limit, int offset);
    void updateAccess(UUID id, AdminRole role, Set<Permission> permissions, Set<String> countries);
    void updateStatus(UUID id, AdminStatus status);
    void updateProfile(UUID id, String firstName, String lastName, String phone);
    void storeInvitation(UUID id, String tokenHash, Instant expiresAt);
    Optional<AdminUser> findByInvitation(String tokenHash, Instant now);
    void completeInvitation(UUID id, String passwordHash);
    boolean deleteInvited(UUID id);
}
