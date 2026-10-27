package com.evcharge.customer.admin.infrastructure;

import com.evcharge.customer.admin.application.AdminSettings;
import com.evcharge.customer.admin.application.port.AdminTokenIssuer;
import com.evcharge.customer.admin.domain.AdminUser;
import com.evcharge.customer.shared.AdminAudience;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class JwtAdminTokenIssuer implements AdminTokenIssuer {

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final AdminSettings settings;
    private final Clock clock;

    JwtAdminTokenIssuer(JwtEncoder encoder, JwtDecoder decoder, AdminSettings settings, Clock clock) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.settings = settings;
        this.clock = clock;
    }

    @Override
    public String issueAccess(AdminUser admin) {
        return sign(admin.id(), AdminAudience.ACCESS, settings.accessTokenTtl());
    }

    @Override
    public Duration accessTimeToLive() {
        return settings.accessTokenTtl();
    }

    @Override
    public String issueChallenge(UUID adminId) {
        return sign(adminId, AdminAudience.CHALLENGE, settings.challengeTtl());
    }

    @Override
    public Optional<UUID> readChallenge(String token) {
        try {
            Jwt jwt = decoder.decode(token);
            if (!AdminAudience.isChallengeToken(jwt)) {
                return Optional.empty();
            }
            return Optional.of(UUID.fromString(jwt.getSubject()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private String sign(UUID adminId, String audience, Duration timeToLive) {
        Instant now = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("zella")
                .subject(adminId.toString())
                .audience(List.of(audience))
                .issuedAt(now)
                .expiresAt(now.plus(timeToLive))
                .build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
