package com.evcharge.customer.wallet.infrastructure.pvit;

import java.util.Map;
import java.util.Optional;

final class PvitCustomerErrors {

    private static final Map<String, String> CODES = Map.of(
            "INVALID_CLIENT_ACCOUNT_NUMBER", "INVALID_ACCOUNT_NUMBER");

    private PvitCustomerErrors() {
    }

    static Optional<String> failureCodeOf(String providerError) {
        return Optional.ofNullable(providerError).map(CODES::get);
    }
}
