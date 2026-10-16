package com.evcharge.customer.catalog.domain;

public final class QrContent {

    private static final String PATH_MARKER = "/c/";

    private QrContent() {
    }

    public static String tokenOf(String scanned) {
        String content = scanned.trim();
        int marker = content.lastIndexOf(PATH_MARKER);
        String token = marker >= 0 ? content.substring(marker + PATH_MARKER.length()) : content;
        int cut = indexOfAny(token, '?', '#', '/');
        return cut >= 0 ? token.substring(0, cut) : token;
    }

    private static int indexOfAny(String value, char... chars) {
        for (int i = 0; i < value.length(); i++) {
            for (char c : chars) {
                if (value.charAt(i) == c) {
                    return i;
                }
            }
        }
        return -1;
    }
}
