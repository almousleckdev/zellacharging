package com.evcharge.customer.wallet.domain;

public record PayoutOutcome(PayoutStatus status, String providerRef, String failureCode) {

    public static PayoutOutcome pending(String providerRef) {
        return new PayoutOutcome(PayoutStatus.PENDING, providerRef, null);
    }

    public static PayoutOutcome paid(String providerRef) {
        return new PayoutOutcome(PayoutStatus.PAID, providerRef, null);
    }

    public static PayoutOutcome failed(String failureCode) {
        return new PayoutOutcome(PayoutStatus.FAILED, null, failureCode);
    }
}
