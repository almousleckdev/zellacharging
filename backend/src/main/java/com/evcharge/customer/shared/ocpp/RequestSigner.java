package com.evcharge.customer.shared.ocpp;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class RequestSigner {

    private static final String HMAC = "HmacSHA256";

    private final byte[] secret;

    public RequestSigner(String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String sign(String method, String path, String query, String timestamp, String nonce, byte[] body) {
        String canonical = String.join("\n", method.toUpperCase(java.util.Locale.ROOT), path,
                query == null ? "" : query, timestamp, nonce, sha256Hex(body));
        try {
            Mac mac = Mac.getInstance(HMAC);
            mac.init(new SecretKeySpec(secret, HMAC));
            return HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String sha256Hex(byte[] body) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(body));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
