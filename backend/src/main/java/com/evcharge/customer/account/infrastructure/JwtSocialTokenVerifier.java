package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.application.port.SocialTokenVerifier;
import com.evcharge.customer.account.domain.SocialProfile;
import com.evcharge.customer.account.domain.SocialProvider;
import com.evcharge.customer.shared.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
class JwtSocialTokenVerifier implements SocialTokenVerifier {

    private final Map<SocialProvider, JwtDecoder> decoders;

    @Autowired
    JwtSocialTokenVerifier(SocialSettings settings) {
        this(configured(settings));
    }

    JwtSocialTokenVerifier(Map<SocialProvider, JwtDecoder> decoders) {
        this.decoders = decoders;
    }

    @Override
    public SocialProfile verify(SocialProvider provider, String idToken) {
        JwtDecoder decoder = decoders.get(provider);
        if (decoder == null) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "SOCIAL_UNAVAILABLE",
                    "This sign-in method is not available");
        }
        try {
            return profileOf(provider, decoder.decode(idToken));
        } catch (JwtException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "SOCIAL_TOKEN_INVALID", "The sign-in token is invalid");
        }
    }

    private static SocialProfile profileOf(SocialProvider provider, Jwt jwt) {
        return new SocialProfile(provider, jwt.getSubject(), jwt.getClaimAsString("email"),
                "true".equalsIgnoreCase(String.valueOf((Object) jwt.getClaim("email_verified"))),
                jwt.getClaimAsString("given_name"), jwt.getClaimAsString("family_name"),
                jwt.getClaimAsString("picture"));
    }

    private static Map<SocialProvider, JwtDecoder> configured(SocialSettings settings) {
        Map<SocialProvider, JwtDecoder> decoders = new EnumMap<>(SocialProvider.class);
        if (!settings.googleClientIds().isEmpty()) {
            decoders.put(SocialProvider.GOOGLE, SocialJwtDecoders.remote(SocialJwtDecoders.GOOGLE_KEYS,
                    SocialJwtDecoders.GOOGLE_ISSUERS, settings.googleClientIds()));
        }
        if (!settings.appleClientIds().isEmpty()) {
            decoders.put(SocialProvider.APPLE, SocialJwtDecoders.remote(SocialJwtDecoders.APPLE_KEYS,
                    SocialJwtDecoders.APPLE_ISSUERS, settings.appleClientIds()));
        }
        return decoders;
    }
}
