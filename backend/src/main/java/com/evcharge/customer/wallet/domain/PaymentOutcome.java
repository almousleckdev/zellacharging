package com.evcharge.customer.wallet.domain;

public record PaymentOutcome(TopupState state, String providerRef, String failureCode) {

    public static PaymentOutcome succeeded(String providerRef) {
        return new PaymentOutcome(TopupState.SUCCEEDED, providerRef, null);
    }

    public static PaymentOutcome pending(String providerRef) {
        return new PaymentOutcome(TopupState.PENDING, providerRef, null);
    }

    public static PaymentOutcome failed(String failureCode) {
        return new PaymentOutcome(TopupState.FAILED, null, failureCode);
    }
}
