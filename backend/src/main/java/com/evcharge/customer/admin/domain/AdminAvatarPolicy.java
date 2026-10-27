package com.evcharge.customer.admin.domain;

import java.util.Optional;

public final class AdminAvatarPolicy {

    public static final int MAX_BYTES = 1_000_000;

    private AdminAvatarPolicy() {
    }

    public static Optional<String> acceptedType(String declaredType, byte[] data) {
        if (data == null || data.length == 0 || data.length > MAX_BYTES) {
            return Optional.empty();
        }
        String declared = essence(declaredType);
        return Optional.ofNullable(sniff(data)).filter(declared::equals);
    }

    private static String sniff(byte[] data) {
        if (startsWith(data, 0xFF, 0xD8, 0xFF)) {
            return "image/jpeg";
        }
        if (startsWith(data, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)) {
            return "image/png";
        }
        if (data.length > 12 && startsWith(data, 'R', 'I', 'F', 'F') && data[8] == 'W' && data[9] == 'E'
                && data[10] == 'B' && data[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    private static boolean startsWith(byte[] data, int... prefix) {
        if (data.length < prefix.length) {
            return false;
        }
        for (int index = 0; index < prefix.length; index++) {
            if ((data[index] & 0xFF) != prefix[index]) {
                return false;
            }
        }
        return true;
    }

    private static String essence(String contentType) {
        if (contentType == null) {
            return "";
        }
        int parameters = contentType.indexOf(';');
        return (parameters < 0 ? contentType : contentType.substring(0, parameters)).trim().toLowerCase();
    }
}
