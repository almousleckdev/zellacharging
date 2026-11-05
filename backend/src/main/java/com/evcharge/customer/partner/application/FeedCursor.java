package com.evcharge.customer.partner.application;

import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

public record FeedCursor(Instant updatedAt, UUID sessionId) {

    public static final FeedCursor START = new FeedCursor(Instant.EPOCH, new UUID(0, 0));

    public static String encode(Instant updatedAt, UUID sessionId) {
        long micros = ChronoUnit.MICROS.between(Instant.EPOCH, updatedAt);
        String raw = micros + ":" + sessionId;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static FeedCursor decode(String cursor) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            int separator = raw.indexOf(':');
            long micros = Long.parseLong(raw.substring(0, separator));
            return new FeedCursor(Instant.EPOCH.plus(micros, ChronoUnit.MICROS),
                    UUID.fromString(raw.substring(separator + 1)));
        } catch (RuntimeException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PARTNER_CURSOR_INVALID", "The cursor is not valid");
        }
    }
}
