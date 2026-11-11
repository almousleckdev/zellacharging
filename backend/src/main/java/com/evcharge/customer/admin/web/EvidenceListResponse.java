package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminEvidence;
import com.evcharge.customer.admin.application.EvidenceListing;
import com.evcharge.customer.shared.SuspensionEvidenceRow;
import com.evcharge.customer.shared.SuspensionEvidenceRow.Person;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

record EvidenceListResponse(long total, int page, int size, List<Item> items) {

    record Item(String country, UUID eventId, String kind, Instant occurredAt, long amountMinor, String currency,
                boolean receivingLinkedDriver, Person suspended, Person receiving) {

        static Item of(AdminEvidence evidence) {
            SuspensionEvidenceRow row = evidence.row();
            return new Item(evidence.country(), row.eventId(), row.kind(), row.occurredAt(), row.amountMinor(),
                    row.currency(), row.receivingLinked(), row.suspended(), row.receiving());
        }
    }

    static EvidenceListResponse from(EvidenceListing listing) {
        return new EvidenceListResponse(listing.total(), listing.page(), listing.size(),
                listing.items().stream().map(Item::of).toList());
    }
}
