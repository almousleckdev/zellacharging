package com.evcharge.customer.partner.web;

import com.evcharge.customer.partner.domain.EvidenceItem;

import java.time.Instant;
import java.util.UUID;

record EvidenceItemView(UUID eventId, UUID suspendedCustomerId, String kind, String receivingAccountRef,
                        boolean receivingIsLinkedDriver, UUID receivingCustomerId, long amountMinor,
                        String currency, Instant occurredAt) {

    static EvidenceItemView of(EvidenceItem item) {
        return new EvidenceItemView(item.eventId(), item.suspendedCustomerId(), item.kind(), item.receivingRef(),
                item.receivingLinked(), item.receivingCustomerId(), item.amountMinor(), item.currency(),
                item.occurredAt());
    }
}
