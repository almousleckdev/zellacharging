package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.CountryComplimentary;

import java.time.Instant;
import java.util.UUID;

record ComplimentaryResponse(UUID customerId, String country, String firstName, String lastName, String reason,
                             UUID grantedBy, Instant grantedAt, Instant expiresAt) {

    static ComplimentaryResponse from(CountryComplimentary entry) {
        var grant = entry.grant();
        return new ComplimentaryResponse(grant.customerId(), entry.country(), grant.firstName(), grant.lastName(),
                grant.reason(), grant.grantedBy(), grant.grantedAt(), grant.expiresAt());
    }
}
