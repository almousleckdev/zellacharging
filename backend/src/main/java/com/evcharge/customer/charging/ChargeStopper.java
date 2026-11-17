package com.evcharge.customer.charging;

import java.util.UUID;

public interface ChargeStopper {

    void stop(UUID sessionId);

    void stopTransaction(int transactionId);
}
