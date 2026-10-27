package com.evcharge.customer.wallet.infrastructure.pvit;

import com.evcharge.customer.wallet.domain.MobileOperator;

import java.util.Map;
import java.util.Optional;

final class PvitOperators {

    private static final String GABON = "GA";
    private static final Map<MobileOperator, String> GABON_CODES = Map.of(
            MobileOperator.AIRTEL, "AIRTEL_MONEY",
            MobileOperator.MOOV, "MOOV_MONEY");

    private PvitOperators() {
    }

    static Optional<String> codeOf(String country, MobileOperator operator) {
        return GABON.equals(country) ? Optional.ofNullable(GABON_CODES.get(operator)) : Optional.empty();
    }
}
