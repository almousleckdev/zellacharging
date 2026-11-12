package com.evcharge.customer.wallet.domain;

import java.util.UUID;

public record FundingHold(UUID sessionId, UUID customerId, long reservedMinor, HoldState state, boolean waived) {

    public boolean isOpen() {
        return state == HoldState.OPEN;
    }
}
