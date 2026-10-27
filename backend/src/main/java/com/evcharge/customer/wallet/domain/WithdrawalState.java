package com.evcharge.customer.wallet.domain;

public enum WithdrawalState {
    REQUESTED,
    PROCESSING,
    PAID,
    FAILED,
    REVIEW;

    public boolean isUnsettled() {
        return this == REQUESTED || this == PROCESSING;
    }
}
