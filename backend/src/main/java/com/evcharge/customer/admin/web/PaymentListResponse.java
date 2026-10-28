package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.CountryEntry;
import com.evcharge.customer.admin.application.CountryListing;
import com.evcharge.customer.wallet.PaymentRecord;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

record PaymentListResponse(long total, int page, int size, List<Item> items) {

    static PaymentListResponse from(CountryListing<PaymentRecord> listing) {
        return new PaymentListResponse(listing.total(), listing.page(), listing.size(),
                listing.items().stream().map(Item::from).toList());
    }

    record Item(String kind, UUID id, String country, UUID customerId, String customerName, String operator,
                String msisdn, long amountMinor, String currency, int decimals, String state, String failureCode,
                String reference, Instant createdAt) {

        static Item from(CountryEntry<PaymentRecord> entry) {
            PaymentRecord p = entry.item();
            return new Item(p.kind(), p.id(), entry.country(), p.customerId(), p.customerName(), p.operator(),
                    p.msisdn(), p.amountMinor(), p.currency(), p.decimals(), p.state(), p.failureCode(),
                    p.reference(), p.createdAt());
        }
    }
}
