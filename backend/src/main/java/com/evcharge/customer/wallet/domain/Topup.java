package com.evcharge.customer.wallet.domain;

import com.evcharge.customer.shared.TenantContext;

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
        String reference,
        Instant createdAt,
        Instant updatedAt) {

    public static Topup pending(UUID customerId, MobileOperator operator, String msisdn, long amountMinor,
                                String idempotencyKey, Instant now) {
        UUID id = UUID.randomUUID();
        return new Topup(id, customerId, operator, msisdn, amountMinor, TopupState.PENDING, null, null,
                idempotencyKey, TopupReference.forPayment(TenantContext.require()), now, now);
    }
}
