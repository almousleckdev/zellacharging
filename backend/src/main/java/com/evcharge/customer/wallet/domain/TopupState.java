package com.evcharge.customer.wallet.domain;

public enum TopupState {
    PENDING,
    SUCCEEDED,
    FAILED,
    EXPIRED;

    public boolean isUnsettled() {
        return this == PENDING || this == EXPIRED;
    }
}
