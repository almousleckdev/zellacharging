package com.evcharge.customer.charging.application.port;

import java.util.UUID;

public interface TransactionClaims {

    boolean heldByAnother(int transactionId, UUID sessionId);

    boolean knownIdTag(String idTag);
}
