package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminRefreshTokenRepository;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.domain.AdminPasswordPolicy;
import com.evcharge.customer.admin.domain.AdminUser;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class AdminPasswordService {

    private final AdminRepository admins;
    private final AdminRefreshTokenRepository refreshTokens;
    private final AdminPasswordHasher passwords;
    private final AdminFailures failures;
    private final AdminAudit audit;
    private final Clock clock;

    public AdminPasswordService(AdminRepository admins, AdminRefreshTokenRepository refreshTokens,
                                AdminPasswordHasher passwords, AdminFailures failures, AdminAudit audit,
                                Clock clock) {
        this.admins = admins;
        this.refreshTokens = refreshTokens;
        this.passwords = passwords;
        this.failures = failures;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional(noRollbackFor = ApiException.class)
    public void change(UUID adminId, String currentPassword, String newPassword) {
        AdminUser admin = admins.findById(adminId).filter(AdminUser::active)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED",
                        "Authentication required"));
        failures.requireNotLocked(admin);
        if (!passwords.matches(currentPassword, admin.passwordHash())) {
            failures.register(admin);
            audit.record(adminId, null, "ADMIN_PASSWORD_CHANGE_REFUSED", "admin", null);
            throw badRequest("INVALID_CURRENT_PASSWORD");
        }
        if (!AdminPasswordPolicy.accepts(newPassword)) {
            throw badRequest("WEAK_PASSWORD");
        }
        if (passwords.matches(newPassword, admin.passwordHash())) {
            throw badRequest("PASSWORD_UNCHANGED");
        }
        admins.updatePassword(adminId, passwords.hash(newPassword));
        admins.clearFailures(adminId);
        refreshTokens.revokeAllOf(adminId, clock.instant());
        audit.record(adminId, null, "ADMIN_PASSWORD_CHANGED", "admin", null);
    }

    private static ApiException badRequest(String code) {
        return new ApiException(HttpStatus.BAD_REQUEST, code, "Password change refused");
    }
}
