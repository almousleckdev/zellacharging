package com.evcharge.customer.wallet;

import com.evcharge.customer.shared.Money;

import java.util.List;
import java.util.UUID;

public interface WalletFunding {

    Money reserve(UUID customerId, UUID sessionId, long amountMinor);

    void settle(UUID sessionId, Long costMinor, String currency);

    void release(UUID sessionId);

    List<UUID> openHoldSessions();

    Money reserved(UUID sessionId);
}
