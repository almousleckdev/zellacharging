package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.AccessTokenIssuer;
import com.evcharge.customer.account.application.port.RefreshTokenRepository;
import com.evcharge.customer.account.domain.RefreshToken;
import com.evcharge.customer.account.domain.Sha256;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import java.util.UUID;

@Component
public class SessionIssuer {

    private final SecureRandom random = new SecureRandom();
    private final AccessTokenIssuer accessTokens;
    private final RefreshTokenRepository refreshTokens;
    private final AccountSettings settings;
    private final Clock clock;

    public SessionIssuer(AccessTokenIssuer accessTokens, RefreshTokenRepository refreshTokens,
                         AccountSettings settings, Clock clock) {
        this.accessTokens = accessTokens;
        this.refreshTokens = refreshTokens;
        this.settings = settings;
        this.clock = clock;
    }

    public AuthTokens issue(UUID customerId, UUID familyId) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        refreshTokens.save(new RefreshToken(UUID.randomUUID(), customerId, familyId, Sha256.hex(raw),
                clock.instant().plus(settings.security().refreshTokenTtl()), null, null));
        return new AuthTokens(accessTokens.issue(customerId), raw, accessTokens.timeToLive().toSeconds());
    }
}
