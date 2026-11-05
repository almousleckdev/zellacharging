package com.evcharge.customer.charging.application;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;

public final class WebhookSignature {

    private static final String HMAC = "HmacSHA256";

    private WebhookSignature() {
    }

    public static boolean valid(byte[] body, String signature, String timestamp, String secret, Instant now,
                                Duration tolerance) {
        if (signature == null || signature.isBlank() || secret == null || secret.isBlank()) {
            return false;
        }
        byte[] signed = timestamp == null ? body : timestamped(body, timestamp, now, tolerance);
        if (signed == null) {
            return false;
        }
        byte[] expected = HexFormat.of().formatHex(hmac(secret, signed)).getBytes(StandardCharsets.UTF_8);
        byte[] given = signature.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, given);
    }

    private static byte[] timestamped(byte[] body, String timestamp, Instant now, Duration tolerance) {
        long seconds;
        try {
            seconds = Long.parseLong(timestamp.trim());
        } catch (NumberFormatException e) {
            return null;
        }
        if (Duration.between(Instant.ofEpochSecond(seconds), now).abs().compareTo(tolerance) > 0) {
            return null;
        }
        byte[] prefix = (seconds + "\n").getBytes(StandardCharsets.UTF_8);
        byte[] joined = new byte[prefix.length + body.length];
        System.arraycopy(prefix, 0, joined, 0, prefix.length);
        System.arraycopy(body, 0, joined, prefix.length, body.length);
        return joined;
    }

    private static byte[] hmac(String secret, byte[] data) {
        try {
            Mac mac = Mac.getInstance(HMAC);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC));
            return mac.doFinal(data);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
