package com.evcharge.customer.account.web;

import com.evcharge.customer.account.domain.Customer;

import java.util.UUID;

record ProfileResponse(UUID id, String firstName, String lastName, String email, String phone, String status) {

    static ProfileResponse from(Customer customer) {
        return new ProfileResponse(customer.id(), customer.firstName(), customer.lastName(),
                customer.email(), customer.phone(), customer.status().name());
    }
}
