package com.evcharge.customer.partner.domain;

import java.time.Instant;
import java.util.UUID;

public record EvidenceItem(long sequence, UUID eventId, UUID suspendedCustomerId, String kind,
                           String receivingRef, boolean receivingLinked, UUID receivingCustomerId,
                           long amountMinor, String currency, Instant occurredAt) {
}
