package com.evcharge.customer.wallet.web;

import com.evcharge.customer.wallet.domain.Topup;

import java.time.Instant;
import java.util.UUID;

record TopupResponse(UUID id, String state, long amountMinor, String operator, String failureCode,
                     Instant createdAt) {

    static TopupResponse from(Topup topup) {
        return new TopupResponse(topup.id(), topup.state().name(), topup.amountMinor(), topup.operator().name(),
                topup.failureCode(), topup.createdAt());
    }
}
