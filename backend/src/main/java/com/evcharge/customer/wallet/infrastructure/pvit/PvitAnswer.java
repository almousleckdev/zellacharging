package com.evcharge.customer.wallet.infrastructure.pvit;

import java.util.regex.Pattern;

final class PvitAnswer {

    private static final Pattern LONG_NUMBER = Pattern.compile("\\d{8,}");
    private static final int LIMIT = 500;

    private PvitAnswer() {
    }

    static String masked(String body) {
        String hidden = LONG_NUMBER.matcher(body == null ? "" : body).replaceAll("***");
        return hidden.length() <= LIMIT ? hidden : hidden.substring(0, LIMIT);
    }
}
