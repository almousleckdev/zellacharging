package com.evcharge.customer.shared;

import java.time.Instant;
import java.util.UUID;

public record SuspensionEvidenceRow(UUID eventId, String kind, Instant occurredAt, long amountMinor, String currency,
                                    boolean receivingLinked, Person suspended, Person receiving) {

    public record Person(UUID id, String name, String phone, String email) {
    }
}
