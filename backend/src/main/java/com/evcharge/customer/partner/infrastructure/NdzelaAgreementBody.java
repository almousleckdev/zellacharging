package com.evcharge.customer.partner.infrastructure;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
record NdzelaAgreementBody(UUID customerId, LocalDate startsOn, LocalDate endsOn, String conditions) {
}
