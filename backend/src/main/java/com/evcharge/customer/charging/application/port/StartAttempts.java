package com.evcharge.customer.charging.application.port;

import java.util.Set;
import java.util.UUID;

public interface StartAttempts {

    Set<Integer> retired(UUID sessionId);

    void retire(UUID sessionId, int transactionId);
}
