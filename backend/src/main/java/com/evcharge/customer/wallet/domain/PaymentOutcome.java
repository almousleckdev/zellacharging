package com.evcharge.customer.wallet.domain;

public record PaymentOutcome(TopupState state, String providerRef, Long confirmedMinor, String failureCode) {

    public static PaymentOutcome succeeded(String providerRef, long confirmedMinor) {
        return new PaymentOutcome(TopupState.SUCCEEDED, providerRef, confirmedMinor, null);
    }

    public static PaymentOutcome pending(String providerRef) {
        return new PaymentOutcome(TopupState.PENDING, providerRef, null, null);
    }

    public static PaymentOutcome failed(String failureCode) {
        return new PaymentOutcome(TopupState.FAILED, null, null, failureCode);
    }
}
