package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.WebhookSignature;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookSignatureTest {

    private static final String SECRET = "whsec-test";
    private static final Instant NOW = Instant.parse("2026-11-05T18:00:00Z");
    private static final Duration TOLERANCE = Duration.ofSeconds(300);
    private static final byte[] BODY = "{\"eventId\":\"e1\"}".getBytes(StandardCharsets.UTF_8);

    private static String hmac(String secret, String text) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(text.getBytes(StandardCharsets.UTF_8)));
    }

    private static String v2(String secret, long timestamp) throws Exception {
        return hmac(secret, timestamp + "\n" + new String(BODY, StandardCharsets.UTF_8));
    }

    @Test
    void acceptsTheBodyOnlySignature() throws Exception {
        String signature = hmac(SECRET, new String(BODY, StandardCharsets.UTF_8));

        assertThat(WebhookSignature.valid(BODY, signature, null, SECRET, NOW, TOLERANCE)).isTrue();
    }

    @Test
    void acceptsTheTimestampedSignatureWithinTheWindow() throws Exception {
        long timestamp = NOW.minusSeconds(100).getEpochSecond();

        assertThat(WebhookSignature.valid(BODY, v2(SECRET, timestamp), Long.toString(timestamp), SECRET, NOW,
                TOLERANCE)).isTrue();
    }

    @Test
    void rejectsATimestampedSignatureOutsideTheWindow() throws Exception {
        long old = NOW.minusSeconds(301).getEpochSecond();
        long future = NOW.plusSeconds(301).getEpochSecond();

        assertThat(WebhookSignature.valid(BODY, v2(SECRET, old), Long.toString(old), SECRET, NOW, TOLERANCE)).isFalse();
        assertThat(WebhookSignature.valid(BODY, v2(SECRET, future), Long.toString(future), SECRET, NOW, TOLERANCE))
                .isFalse();
    }

    @Test
    void rejectsAnotherSecretATamperedBodyAndMissingOrBadInput() throws Exception {
        long timestamp = NOW.getEpochSecond();
        String good = v2(SECRET, timestamp);
        byte[] tampered = "{\"eventId\":\"e2\"}".getBytes(StandardCharsets.UTF_8);

        assertThat(WebhookSignature.valid(BODY, v2("other", timestamp), Long.toString(timestamp), SECRET, NOW,
                TOLERANCE)).isFalse();
        assertThat(WebhookSignature.valid(tampered, good, Long.toString(timestamp), SECRET, NOW, TOLERANCE)).isFalse();
        assertThat(WebhookSignature.valid(BODY, null, null, SECRET, NOW, TOLERANCE)).isFalse();
        assertThat(WebhookSignature.valid(BODY, good, "not-a-number", SECRET, NOW, TOLERANCE)).isFalse();
        assertThat(WebhookSignature.valid(BODY, good, Long.toString(timestamp), "", NOW, TOLERANCE)).isFalse();
        assertThat(WebhookSignature.valid(BODY, good, Long.toString(timestamp), null, NOW, TOLERANCE)).isFalse();
    }
}
