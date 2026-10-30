package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.domain.SocialProfile;
import com.evcharge.customer.account.domain.SocialProvider;
import com.evcharge.customer.shared.ApiException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtSocialTokenVerifierTest {

    private static KeyPair keys;
    private static JwtSocialTokenVerifier verifier;

    @BeforeAll
    static void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keys = generator.generateKeyPair();
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) keys.getPublic()).build();
        verifier = new JwtSocialTokenVerifier(Map.of(SocialProvider.GOOGLE,
                SocialJwtDecoders.validated(decoder, SocialJwtDecoders.GOOGLE_ISSUERS, List.of("zella-client"))));
    }

    private static String token(String issuer, String audience, Instant expires, Object emailVerified)
            throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder().issuer(issuer).audience(audience).subject("sub-1")
                .claim("email", "aline@example.com").claim("email_verified", emailVerified)
                .claim("given_name", "Aline").claim("family_name", "Obame")
                .issueTime(new Date()).expirationTime(Date.from(expires)).build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
        jwt.sign(new RSASSASigner((RSAPrivateKey) keys.getPrivate()));
        return jwt.serialize();
    }

    @Test
    void aValidTokenGivesTheProfile() throws Exception {
        SocialProfile profile = verifier.verify(SocialProvider.GOOGLE,
                token("https://accounts.google.com", "zella-client", Instant.now().plusSeconds(600), true));

        assertThat(profile.subject()).isEqualTo("sub-1");
        assertThat(profile.email()).isEqualTo("aline@example.com");
        assertThat(profile.emailVerified()).isTrue();
        assertThat(profile.firstName()).isEqualTo("Aline");
    }

    @Test
    void appleStyleStringBooleansAreUnderstood() throws Exception {
        SocialProfile profile = verifier.verify(SocialProvider.GOOGLE,
                token("accounts.google.com", "zella-client", Instant.now().plusSeconds(600), "true"));

        assertThat(profile.emailVerified()).isTrue();
    }

    @Test
    void wrongAudienceWrongIssuerAndExpiredTokensAreRefused() throws Exception {
        for (String bad : Set.of(
                token("https://accounts.google.com", "someone-else", Instant.now().plusSeconds(600), true),
                token("https://evil.example.com", "zella-client", Instant.now().plusSeconds(600), true),
                token("https://accounts.google.com", "zella-client", Instant.now().minusSeconds(600), true),
                "not-a-token")) {
            assertThatThrownBy(() -> verifier.verify(SocialProvider.GOOGLE, bad))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).code()).isEqualTo("SOCIAL_TOKEN_INVALID");
        }
    }

    @Test
    void aProviderWithoutClientIdsIsUnavailable() {
        assertThatThrownBy(() -> verifier.verify(SocialProvider.APPLE, "x")).isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).code()).isEqualTo("SOCIAL_UNAVAILABLE");
    }
}
