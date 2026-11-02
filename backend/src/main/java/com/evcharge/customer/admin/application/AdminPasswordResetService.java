package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.AdminAccountRepository;
import com.evcharge.customer.admin.application.port.AdminMailer;
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
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@Service
public class AdminPasswordResetService {

    private static final Duration LINK_TTL = Duration.ofHours(1);

    private final AdminRepository admins;
    private final AdminAccountRepository accounts;
    private final AdminRefreshTokenRepository refreshTokens;
    private final AdminPasswordHasher passwords;
    private final AdminMailer mailer;
    private final InvitationTokens tokens;
    private final AdminSettings settings;
    private final AdminAudit audit;
    private final Clock clock;

    public AdminPasswordResetService(AdminRepository admins, AdminAccountRepository accounts,
                                     AdminRefreshTokenRepository refreshTokens, AdminPasswordHasher passwords,
                                     AdminMailer mailer, InvitationTokens tokens, AdminSettings settings,
                                     AdminAudit audit, Clock clock) {
        this.admins = admins;
        this.accounts = accounts;
        this.refreshTokens = refreshTokens;
        this.passwords = passwords;
        this.mailer = mailer;
        this.tokens = tokens;
        this.settings = settings;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional
    public void request(String email) {
        admins.findByEmail(email.trim().toLowerCase(Locale.ROOT)).filter(AdminUser::active).ifPresent(admin -> {
            String token = tokens.newToken();
            Instant expiresAt = clock.instant().plus(LINK_TTL);
            accounts.storePasswordReset(admin.id(), tokens.hash(token), expiresAt);
            String base = settings.dashboardUrl().replaceAll("/+$", "");
            mailer.sendPasswordReset(admin.email(), base + "/reset-password?token=" + token, expiresAt);
            audit.record(admin.id(), null, "ADMIN_PASSWORD_RESET_REQUESTED", admin.id().toString(), null);
        });
    }

    @Transactional
    public void reset(String token, String newPassword) {
        AdminUser admin = accounts.findByPasswordReset(tokens.hash(token), clock.instant())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "RESET_LINK_INVALID",
                        "The reset link is invalid or has expired"));
        if (!AdminPasswordPolicy.accepts(newPassword)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "WEAK_PASSWORD", "Password too weak");
        }
        accounts.completePasswordReset(admin.id(), passwords.hash(newPassword));
        refreshTokens.revokeAllOf(admin.id(), clock.instant());
        audit.record(admin.id(), null, "ADMIN_PASSWORD_RESET", admin.id().toString(), null);
    }
}
