package com.evcharge.customer.account.application;

import java.util.Optional;

final class RefreshTokenFormat {

    static final String SEPARATOR = ".";

    private RefreshTokenFormat() {
    }

    static Optional<String> countryOf(String rawToken) {
        int cut = rawToken == null ? -1 : rawToken.indexOf(SEPARATOR);
        return cut == 2 ? Optional.of(rawToken.substring(0, cut)) : Optional.empty();
    }
}
