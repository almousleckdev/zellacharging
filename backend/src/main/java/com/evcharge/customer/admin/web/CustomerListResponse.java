package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.CountryCustomer;
import com.evcharge.customer.admin.application.CustomerListing;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

record CustomerListResponse(long total, int page, int size, List<Item> items) {

    static CustomerListResponse from(CustomerListing listing) {
        return new CustomerListResponse(listing.total(), listing.page(), listing.size(),
                listing.items().stream().map(Item::from).toList());
    }

    record Item(UUID id, String country, String firstName, String lastName, String email, String phone,
                String status, Instant createdAt) {

        static Item from(CountryCustomer entry) {
            var customer = entry.customer();
            return new Item(customer.id(), entry.country(), customer.firstName(), customer.lastName(),
                    customer.email(), customer.phone(), customer.status(), customer.createdAt());
        }
    }
}
