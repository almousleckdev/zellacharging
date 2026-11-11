package com.evcharge.customer.partner.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

public final class EvidenceReference {

    private EvidenceReference() {
    }

    public static String of(UUID customerId) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(("evidence:" + customerId).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
