package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.application.port.AdminTokenIssuer;
import com.evcharge.customer.admin.application.port.SecretCipher;
import com.evcharge.customer.admin.application.port.TotpAuthenticator;
import com.evcharge.customer.admin.domain.AdminUser;
import com.evcharge.customer.shared.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.OptionalLong;
import java.util.UUID;

@Service
public class AdminTotpService {

    private final AdminRepository admins;
    private final AdminTokenIssuer challenges;
    private final TotpAuthenticator totp;
    private final SecretCipher cipher;
    private final AdminFailures failures;
    private final AdminSessionIssuer sessions;
    private final AdminAudit audit;
    private final Clock clock;

    public AdminTotpService(AdminRepository admins, AdminTokenIssuer challenges, TotpAuthenticator totp,
                            SecretCipher cipher, AdminFailures failures, AdminSessionIssuer sessions,
                            AdminAudit audit, Clock clock) {
        this.admins = admins;
        this.challenges = challenges;
        this.totp = totp;
        this.cipher = cipher;
        this.failures = failures;
        this.sessions = sessions;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional(noRollbackFor = ApiException.class)
    public AdminTokens complete(String challengeToken, String code) {
        UUID adminId = challenges.readChallenge(challengeToken).orElseThrow(AdminFailures::invalidCredentials);
        AdminUser admin = admins.findById(adminId).filter(AdminUser::active)
                .orElseThrow(AdminFailures::invalidCredentials);
        failures.requireNotLocked(admin);
        String encrypted = admin.hasTotp() ? admin.totpSecret() : admin.totpPendingSecret();
        if (encrypted == null) {
            throw AdminFailures.invalidCredentials();
        }
        OptionalLong step = totp.verify(cipher.decrypt(encrypted), code, clock.instant());
        if (step.isEmpty() || !consume(admin, encrypted, step.getAsLong())) {
            failures.register(admin);
            throw AdminFailures.invalidCode();
        }
        admins.clearFailures(admin.id());
        admins.recordLogin(admin.id(), clock.instant());
        audit.record(admin.id(), null, "ADMIN_LOGIN", "admin", admin.hasTotp() ? null : "TOTP_ENROLLED");
        return sessions.issue(admin, UUID.randomUUID());
    }

    private boolean consume(AdminUser admin, String encrypted, long step) {
        return admin.hasTotp() ? admins.advanceTotpStep(admin.id(), step)
                : admins.activateTotp(admin.id(), encrypted, step);
    }
}
