package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.AdminRefreshTokenRepository;
import com.evcharge.customer.admin.application.port.AdminTokenIssuer;
import com.evcharge.customer.admin.domain.AdminRefreshToken;
import com.evcharge.customer.admin.domain.AdminUser;
import com.evcharge.customer.shared.Sha256;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import java.util.UUID;

@Component
class AdminSessionIssuer {

    private final SecureRandom random = new SecureRandom();
    private final AdminTokenIssuer accessTokens;
    private final AdminRefreshTokenRepository refreshTokens;
    private final AdminSettings settings;
    private final Clock clock;

    AdminSessionIssuer(AdminTokenIssuer accessTokens, AdminRefreshTokenRepository refreshTokens,
                       AdminSettings settings, Clock clock) {
        this.accessTokens = accessTokens;
        this.refreshTokens = refreshTokens;
        this.settings = settings;
        this.clock = clock;
    }

    AdminTokens issue(AdminUser admin, UUID familyId) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        refreshTokens.save(new AdminRefreshToken(UUID.randomUUID(), admin.id(), familyId, Sha256.hex(raw),
                clock.instant().plus(settings.refreshTokenTtl()), null, null));
        return new AdminTokens(accessTokens.issueAccess(admin), raw, accessTokens.accessTimeToLive().toSeconds());
    }
}
