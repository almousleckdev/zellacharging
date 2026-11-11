package com.evcharge.customer.partner.application;

import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

final class EvidenceCursor {

    private EvidenceCursor() {
    }

    static String encode(long sequence) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(("e:" + sequence).getBytes(StandardCharsets.UTF_8));
    }

    static long decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            if (!raw.startsWith("e:")) {
                throw new IllegalArgumentException();
            }
            return Long.parseLong(raw.substring(2));
        } catch (RuntimeException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PARTNER_CURSOR_INVALID", "The cursor is not valid");
        }
    }
}
