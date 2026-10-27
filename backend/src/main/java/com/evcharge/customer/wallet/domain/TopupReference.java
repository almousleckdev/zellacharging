package com.evcharge.customer.wallet.domain;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.regex.Pattern;

public final class TopupReference {

    public static final int MAX_LENGTH = 20;

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int PAYMENT_RANDOM_CHARS = MAX_LENGTH - 2;
    private static final int PAYOUT_RANDOM_CHARS = MAX_LENGTH - 3;
    private static final Pattern FORMAT = Pattern.compile("^[A-Z]{2}[A-Z2-7]{" + PAYMENT_RANDOM_CHARS + "}$");
    private static final SecureRandom RANDOM = new SecureRandom();

    private TopupReference() {
    }

    public static String forPayment(String country) {
        return country + randomChars(PAYMENT_RANDOM_CHARS);
    }

    public static String forWithdrawal(String country) {
        return "W" + country + randomChars(PAYOUT_RANDOM_CHARS);
    }

    public static Optional<String> countryOf(String reference) {
        return reference != null && FORMAT.matcher(reference).matches()
                ? Optional.of(reference.substring(0, 2)) : Optional.empty();
    }

    private static String randomChars(int count) {
        StringBuilder text = new StringBuilder(count);
        for (int index = 0; index < count; index++) {
            text.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return text.toString();
    }
}
