package com.evcharge.customer.account.web;

import com.evcharge.customer.account.application.ProfileView;
import com.evcharge.customer.account.domain.Customer;

import java.util.UUID;

record ProfileResponse(UUID id, String firstName, String lastName, String email, String phone, String status,
                       boolean emailVerified, boolean phoneVerified, boolean hasPassword) {

    static ProfileResponse from(ProfileView view) {
        Customer customer = view.customer();
        return new ProfileResponse(customer.id(), customer.firstName(), customer.lastName(), customer.email(),
                customer.phone(), customer.status().name(), view.emailVerified(), view.phoneVerified(),
                view.hasPassword());
    }
}
