package com.evcharge.customer.account.web;

import com.evcharge.customer.account.domain.CodeDelivery;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

record ForgotPasswordRequest(@Size(max = 254) String identifier, @Size(max = 254) String email,
                             CodeDelivery delivery) {

    String contact() {
        return identifier != null && !identifier.isBlank() ? identifier : email;
    }

    @AssertTrue
    boolean hasContact() {
        return contact() != null && !contact().isBlank();
    }
}
