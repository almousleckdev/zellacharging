package com.evcharge.customer.notification.infrastructure;

import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;

class FcmAccessToken {

    private static final String SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String GRANT = "urn:ietf:params:oauth:grant-type:jwt-bearer";

    private final JsonNode account;
    private final RestClient client;
    private final ObjectMapper mapper;
    private final Clock clock;
    private String cached;
    private Instant expiresAt = Instant.EPOCH;

    FcmAccessToken(JsonNode account, RestClient client, ObjectMapper mapper, Clock clock) {
        this.account = account;
        this.client = client;
        this.mapper = mapper;
        this.clock = clock;
    }

    synchronized String get() {
        Instant now = clock.instant();
        if (cached == null || !now.isBefore(expiresAt.minusSeconds(60))) {
            JsonNode answer = exchange(assertion(now));
            cached = answer.get("access_token").asText();
            expiresAt = now.plusSeconds(answer.get("expires_in").asLong());
        }
        return cached;
    }

    private JsonNode exchange(String assertion) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", GRANT);
        form.add("assertion", assertion);
        String body = client.post().uri(account.get("token_uri").asText())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(String.class);
        return mapper.readTree(body);
    }

    private String assertion(Instant now) {
        String header = encode("{\"alg\":\"RS256\",\"typ\":\"JWT\"}");
        String claims = encode("{\"iss\":\"%s\",\"scope\":\"%s\",\"aud\":\"%s\",\"iat\":%d,\"exp\":%d}".formatted(
                account.get("client_email").asText(), SCOPE, account.get("token_uri").asText(),
                now.getEpochSecond(), now.plusSeconds(3600).getEpochSecond()));
        String unsigned = header + "." + claims;
        return unsigned + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(sign(unsigned));
    }

    private byte[] sign(String data) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey());
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            return signature.sign();
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException("Cannot sign the Firebase access request", e);
        }
    }

    private PrivateKey privateKey() throws java.security.GeneralSecurityException {
        String pem = account.get("private_key").asText().replaceAll("-----[A-Z ]+-----", "").replaceAll("\\s", "");
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pem)));
    }

    private static String encode(String json) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }
}
