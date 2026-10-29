package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.CountryEntry;
import com.evcharge.customer.admin.application.CountryListing;
import com.evcharge.customer.charging.SessionDigest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

record SessionDigestResponse(long total, int page, int size, List<Item> items) {

    static SessionDigestResponse from(CountryListing<SessionDigest> listing) {
        return new SessionDigestResponse(listing.total(), listing.page(), listing.size(),
                listing.items().stream().map(Item::from).toList());
    }

    record Item(UUID customerId, String country, String customerName, long sessions, BigDecimal energyKwh,
                long spentMinor, SessionListResponse.Item last) {

        static Item from(CountryEntry<SessionDigest> entry) {
            SessionDigest digest = entry.item();
            return new Item(digest.last().customerId(), entry.country(), digest.customerName(), digest.sessions(),
                    digest.energyKwh(), digest.spentMinor(),
                    SessionListResponse.Item.from(new CountryEntry<>(entry.country(), digest.last(),
                            digest.last().createdAt())));
        }
    }
}
