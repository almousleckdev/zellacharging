package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.CountryCustomer;
import com.evcharge.customer.admin.application.CustomerListing;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

record CustomerListResponse(long total, int page, int size, List<Item> items) {

    static CustomerListResponse from(CustomerListing listing) {
        return new CustomerListResponse(listing.total(), listing.page(), listing.size(),
                listing.items().stream().map(Item::from).toList());
    }

    record Item(UUID id, String country, String firstName, String lastName, String email, String phone,
                String status, Instant createdAt, Long balanceMinor, String currency, Integer decimals,
                long completedSessions, long spentMinor, BigDecimal energyKwh, Instant lastSessionAt,
                Long avatarVersion) {

        static Item from(CountryCustomer entry) {
            var customer = entry.customer();
            var wallet = entry.summary().wallet();
            var sessions = entry.summary().sessions();
            return new Item(customer.id(), entry.country(), customer.firstName(), customer.lastName(),
                    customer.email(), customer.phone(), customer.status(), customer.createdAt(),
                    wallet == null ? null : wallet.balanceMinor(), wallet == null ? null : wallet.currency(),
                    wallet == null ? null : wallet.decimals(), sessions == null ? 0 : sessions.completed(),
                    sessions == null ? 0 : sessions.spentMinor(),
                    sessions == null ? BigDecimal.ZERO : sessions.energyKwh(),
                    sessions == null ? null : sessions.lastSessionAt(), customer.avatarVersion());
        }
    }
}
