package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.domain.AdminUser;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
class AdminFailures {

    private final AdminRepository admins;
    private final AdminSettings settings;
    private final Clock clock;

    AdminFailures(AdminRepository admins, AdminSettings settings, Clock clock) {
        this.admins = admins;
        this.settings = settings;
        this.clock = clock;
    }

    void requireNotLocked(AdminUser admin) {
        if (admin.isLockedAt(clock.instant())) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "ACCOUNT_LOCKED", "Too many failed attempts");
        }
    }

    void register(AdminUser admin) {
        admins.registerFailure(admin.id(), settings.maxFailedLogins(), clock.instant().plus(settings.lockout()));
    }

    static ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid credentials");
    }

    static ApiException invalidCode() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TOTP_CODE", "Invalid verification code");
    }
}
