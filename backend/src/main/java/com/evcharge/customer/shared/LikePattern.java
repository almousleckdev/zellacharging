package com.evcharge.customer.shared;

public final class LikePattern {

    private LikePattern() {
    }

    public static String contains(String text) {
        return "%" + text.toLowerCase().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }
}
