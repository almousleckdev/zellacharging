package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.application.AccountSettings;
import com.evcharge.customer.account.application.port.AccessTokenIssuer;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
class JwtAccessTokenIssuer implements AccessTokenIssuer {

    private final JwtEncoder encoder;
    private final AccountSettings settings;
    private final Clock clock;

    JwtAccessTokenIssuer(JwtEncoder encoder, AccountSettings settings, Clock clock) {
        this.encoder = encoder;
        this.settings = settings;
        this.clock = clock;
    }

    @Override
    public String issue(UUID customerId) {
        Instant now = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("ndzela")
                .subject(customerId.toString())
                .issuedAt(now)
                .expiresAt(now.plus(timeToLive()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public Duration timeToLive() {
        return settings.security().accessTokenTtl();
    }
}
