package com.evcharge.customer.partner;

import java.time.LocalDate;

public record AgreementTerms(LocalDate startsOn, LocalDate endsOn, String conditions) {
}
