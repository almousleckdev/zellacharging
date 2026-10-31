package com.evcharge.customer.account.infrastructure;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.Collection;
import java.util.Set;

final class SocialJwtDecoders {

    static final String GOOGLE_KEYS = "https://www.googleapis.com/oauth2/v3/certs";
    static final String APPLE_KEYS = "https://appleid.apple.com/auth/keys";
    static final Set<String> GOOGLE_ISSUERS = Set.of("https://accounts.google.com", "accounts.google.com");
    static final Set<String> APPLE_ISSUERS = Set.of("https://appleid.apple.com");

    private SocialJwtDecoders() {
    }

    static JwtDecoder remote(String keysUri, Set<String> issuers, Collection<String> audiences) {
        return validated(NimbusJwtDecoder.withJwkSetUri(keysUri).jwsAlgorithm(SignatureAlgorithm.RS256).build(),
                issuers, audiences);
    }

    static JwtDecoder validated(NimbusJwtDecoder decoder, Set<String> issuers, Collection<String> audiences) {
        OAuth2TokenValidator<Jwt> issuer = jwt -> issuers.contains(jwt.getClaimAsString("iss"))
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Wrong issuer", null));
        OAuth2TokenValidator<Jwt> audience = jwt -> jwt.getAudience() != null
                && jwt.getAudience().stream().anyMatch(audiences::contains)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Wrong audience", null));
        OAuth2TokenValidator<Jwt> defaults = JwtValidators.createDefault();
        decoder.setJwtValidator(token -> {
            OAuth2TokenValidatorResult base = defaults.validate(token);
            if (base.hasErrors()) {
                return base;
            }
            OAuth2TokenValidatorResult claims = issuer.validate(token);
            return claims.hasErrors() ? claims : audience.validate(token);
        });
        return decoder;
    }
}
