package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.application.port.AppleTokenRevoker;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Clock;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
class HttpAppleTokenRevoker implements AppleTokenRevoker {

    private static final Logger log = LoggerFactory.getLogger(HttpAppleTokenRevoker.class);
    private static final String HOST = "https://appleid.apple.com";

    private final SocialSettings settings;
    private final Clock clock;
    private final RestClient http = RestClient.create();

    HttpAppleTokenRevoker(SocialSettings settings, Clock clock) {
        this.settings = settings;
        this.clock = clock;
    }

    @Override
    public void revoke(String authorizationCode) {
        if (authorizationCode == null || authorizationCode.isBlank() || !settings.appleRevocationConfigured()) {
            return;
        }
        try {
            String clientId = settings.appleClientIds().getFirst();
            String secret = clientSecret(clientId);
            Map<?, ?> tokens = post("/auth/token", form(clientId, secret, "code", authorizationCode,
                    "grant_type", "authorization_code"));
            String refresh = tokens == null ? null : (String) tokens.get("refresh_token");
            if (refresh != null) {
                post("/auth/revoke", form(clientId, secret, "token", refresh, "token_type_hint", "refresh_token"));
            }
        } catch (Exception e) {
            log.warn("Apple token revocation failed: {}", e.getMessage());
        }
    }

    private Map<?, ?> post(String path, MultiValueMap<String, String> body) {
        return http.post().uri(HOST + path).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body).retrieve().body(Map.class);
    }

    private static MultiValueMap<String, String> form(String clientId, String secret, String... pairs) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", secret);
        for (int i = 0; i < pairs.length; i += 2) {
            body.add(pairs[i], pairs[i + 1]);
        }
        return body;
    }

    private String clientSecret(String clientId) throws Exception {
        long now = clock.millis();
        JWTClaimsSet claims = new JWTClaimsSet.Builder().issuer(settings.appleTeamId()).subject(clientId)
                .audience(HOST).issueTime(new Date(now)).expirationTime(new Date(now + 300_000)).build();
        SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(settings.appleKeyId()).build(),
                claims);
        jwt.sign(new ECDSASigner(privateKey()));
        return jwt.serialize();
    }

    private ECPrivateKey privateKey() throws Exception {
        String pem = settings.applePrivateKey().replace("\\n", "\n")
                .replaceAll("-----(BEGIN|END) PRIVATE KEY-----", "").replaceAll("\\s", "");
        return (ECPrivateKey) KeyFactory.getInstance("EC")
                .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pem)));
    }
}
