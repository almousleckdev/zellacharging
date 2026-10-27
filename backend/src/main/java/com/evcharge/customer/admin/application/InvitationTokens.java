package com.evcharge.customer.admin.application;

import com.evcharge.customer.shared.Sha256;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
class InvitationTokens {

    private final SecureRandom random = new SecureRandom();

    String newToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    String hash(String token) {
        return Sha256.hex(token);
    }
}
