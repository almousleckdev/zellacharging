package com.evcharge.customer.wallet.domain;

import java.time.Instant;
import java.util.UUID;

public record Topup(
        UUID id,
        UUID customerId,
        MobileOperator operator,
        String msisdn,
        long amountMinor,
        TopupState state,
        String providerRef,
        String failureCode,
        String idempotencyKey,
        Instant createdAt,
        Instant updatedAt) {

    public static Topup pending(UUID customerId, MobileOperator operator, String msisdn, long amountMinor,
                                String idempotencyKey, Instant now) {
        return new Topup(UUID.randomUUID(), customerId, operator, msisdn, amountMinor, TopupState.PENDING,
                null, null, idempotencyKey, now, now);
    }
}
