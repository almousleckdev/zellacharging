package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.CountryEntry;
import com.evcharge.customer.admin.application.CountryListing;
import com.evcharge.customer.wallet.PaymentDigest;

import java.util.List;
import java.util.UUID;

record PaymentDigestResponse(long total, int page, int size, List<Item> items) {

    static PaymentDigestResponse from(CountryListing<PaymentDigest> listing) {
        return new PaymentDigestResponse(listing.total(), listing.page(), listing.size(),
                listing.items().stream().map(Item::from).toList());
    }

    record Item(UUID customerId, String country, String customerName, long operations, long topupsMinor,
                long withdrawalsMinor, PaymentListResponse.Item last) {

        static Item from(CountryEntry<PaymentDigest> entry) {
            PaymentDigest digest = entry.item();
            return new Item(digest.last().customerId(), entry.country(), digest.customerName(), digest.operations(),
                    digest.topupsMinor(), digest.withdrawalsMinor(),
                    PaymentListResponse.Item.from(new CountryEntry<>(entry.country(), digest.last(),
                            digest.last().createdAt())));
        }
    }
}
