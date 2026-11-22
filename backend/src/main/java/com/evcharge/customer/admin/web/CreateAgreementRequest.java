package com.evcharge.customer.admin.web;

import com.evcharge.customer.partner.AgreementTerms;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

record CreateAgreementRequest(@NotNull UUID customerId, @NotNull LocalDate startsOn, LocalDate endsOn,
                              @Size(max = 1000) String conditions) {

    AgreementTerms terms() {
        return new AgreementTerms(startsOn, endsOn, conditions);
    }
}
