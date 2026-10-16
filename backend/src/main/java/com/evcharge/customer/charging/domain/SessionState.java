package com.evcharge.customer.charging.domain;

public enum SessionState {
    REQUESTED,
    START_ACCEPTED,
    ACTIVE,
    STOPPING,
    COMPLETED,
    FAILED,
    ABANDONED;

    public boolean isLive() {
        return this == REQUESTED || this == START_ACCEPTED || this == ACTIVE || this == STOPPING;
    }
}
