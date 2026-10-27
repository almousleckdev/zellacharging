package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.application.port.AdminTokenIssuer;
import com.evcharge.customer.admin.application.port.SecretCipher;
import com.evcharge.customer.admin.application.port.TotpAuthenticator;
import com.evcharge.customer.admin.domain.AdminUser;
import com.evcharge.customer.shared.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminLoginService {

    private final AdminRepository admins;
    private final AdminPasswordHasher passwords;
    private final AdminTokenIssuer tokens;
    private final TotpAuthenticator totp;
    private final SecretCipher cipher;
    private final AdminFailures failures;
    private final AdminSettings settings;
    private final String unknownAdminHash;

    public AdminLoginService(AdminRepository admins, AdminPasswordHasher passwords, AdminTokenIssuer tokens,
                             TotpAuthenticator totp, SecretCipher cipher, AdminFailures failures,
                             AdminSettings settings) {
        this.admins = admins;
        this.passwords = passwords;
        this.tokens = tokens;
        this.totp = totp;
        this.cipher = cipher;
        this.failures = failures;
        this.settings = settings;
        this.unknownAdminHash = passwords.hash(UUID.randomUUID().toString());
    }

    @Transactional(noRollbackFor = ApiException.class)
    public AdminLoginStep login(String email, String password) {
        Optional<AdminUser> known = admins.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .filter(AdminUser::active);
        if (known.isEmpty()) {
            passwords.matches(password, unknownAdminHash);
            throw AdminFailures.invalidCredentials();
        }
        AdminUser admin = known.get();
        failures.requireNotLocked(admin);
        if (!passwords.matches(password, admin.passwordHash())) {
            failures.register(admin);
            throw AdminFailures.invalidCredentials();
        }
        return beginSecondFactor(admin);
    }

    public AdminLoginStep beginSecondFactor(AdminUser admin) {
        String challenge = tokens.issueChallenge(admin.id());
        if (admin.hasTotp()) {
            return AdminLoginStep.required(challenge);
        }
        String secret = totp.newSecret();
        admins.storePendingSecret(admin.id(), cipher.encrypt(secret));
        return AdminLoginStep.enrollment(challenge, secret,
                totp.provisioningUri(settings.totpIssuer(), admin.email(), secret));
    }
}
