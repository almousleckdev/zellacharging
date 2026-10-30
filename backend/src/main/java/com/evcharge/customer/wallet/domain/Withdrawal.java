package com.evcharge.customer.wallet.domain;

import com.evcharge.customer.shared.TenantContext;

import java.time.Instant;
import java.util.UUID;

public record Withdrawal(
        UUID id,
        UUID customerId,
        MobileOperator operator,
        String msisdn,
        long amountMinor,
        WithdrawalState state,
        String reference,
        String providerRef,
        String failureCode,
        String idempotencyKey,
        Instant createdAt,
        Instant updatedAt) {

    public static Withdrawal requested(UUID customerId, MobileOperator operator, String msisdn, long amountMinor,
                                       String idempotencyKey, Instant now) {
        UUID id = UUID.randomUUID();
        return new Withdrawal(id, customerId, operator, msisdn, amountMinor, WithdrawalState.PENDING_APPROVAL,
                TopupReference.forWithdrawal(TenantContext.require()), null, null, idempotencyKey, now, now);
    }
}
