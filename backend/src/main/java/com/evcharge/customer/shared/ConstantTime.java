package com.evcharge.customer.shared;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class ConstantTime {

    private ConstantTime() {
    }

    public static boolean equal(String first, String second) {
        if (first == null || second == null) {
            return false;
        }
        return MessageDigest.isEqual(first.getBytes(StandardCharsets.UTF_8), second.getBytes(StandardCharsets.UTF_8));
    }
}
