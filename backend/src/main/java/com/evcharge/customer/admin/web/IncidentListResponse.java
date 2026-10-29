package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.IncidentListing;
import com.evcharge.customer.admin.application.IncidentView;
import com.evcharge.customer.shared.Incident;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

record IncidentListResponse(long total, long open, long openHigh, int page, int size, List<Item> items) {

    static IncidentListResponse from(IncidentListing listing) {
        return new IncidentListResponse(listing.total(), listing.open(), listing.openHigh(), listing.page(),
                listing.size(), listing.items().stream().map(Item::from).toList());
    }

    record Item(String key, String country, String kind, String severity, String subjectType, UUID subjectId,
                UUID customerId, String customerName, String code, Long amountMinor, String currency, int decimals,
                Instant occurredAt, String status, String note) {

        static Item from(IncidentView view) {
            Incident i = view.incident();
            return new Item(i.key(), view.country(), i.kind(), i.severity(), i.subjectType(), i.subjectId(),
                    i.customerId(), i.customerName(), i.code(), i.amountMinor(), i.currency(), i.decimals(),
                    i.occurredAt(), view.status(), view.note());
        }
    }
}
