package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.CountryEntry;
import com.evcharge.customer.admin.application.CountryListing;
import com.evcharge.customer.charging.SessionRecord;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

record SessionListResponse(long total, int page, int size, List<Item> items) {

    static SessionListResponse from(CountryListing<SessionRecord> listing) {
        return new SessionListResponse(listing.total(), listing.page(), listing.size(),
                listing.items().stream().map(Item::from).toList());
    }

    record Item(UUID id, String country, UUID customerId, String customerName, String stationId, int connectorId,
                String state, String failureCode, BigDecimal energyKwh, Long costMinor, String currency,
                Integer decimals, BigDecimal startBatteryPercent, BigDecimal endBatteryPercent, Instant startedAt,
                Instant stoppedAt, Instant createdAt, String freeCategory, boolean ndzelaDriver) {

        static Item from(CountryEntry<SessionRecord> entry) {
            SessionRecord s = entry.item();
            return new Item(s.id(), entry.country(), s.customerId(), s.customerName(), s.stationId(),
                    s.connectorId(), s.state(), s.failureCode(), s.energyKwh(), s.costMinor(), s.currency(),
                    s.decimals(), s.startBatteryPercent(), s.endBatteryPercent(), s.startedAt(), s.stoppedAt(),
                    s.createdAt(), s.freeCategory(), s.ndzelaDriver());
        }
    }
}
