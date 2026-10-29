package com.evcharge.customer.shared;

import java.time.Instant;
import java.util.UUID;

public record Incident(String kind, String severity, String subjectType, UUID subjectId, UUID customerId,
                       String customerName, String code, Long amountMinor, String currency, int decimals,
                       Instant occurredAt) {

    public String key() {
        return kind + ":" + subjectId;
    }
}
