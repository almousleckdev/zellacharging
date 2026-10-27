package com.evcharge.customer.charging.web;

import com.evcharge.customer.shared.Money;

import java.util.Optional;

record EstimatedCostResponse(long minor, String currency, int decimals) {

    static EstimatedCostResponse from(Optional<Money> estimate) {
        return estimate.map(m -> new EstimatedCostResponse(m.minor(), m.currency(), m.decimals())).orElse(null);
    }
}
