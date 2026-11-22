package com.evcharge.customer.partner;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FreeAgreement(UUID id, UUID driverId, String driverName, LocalDate startsOn, LocalDate endsOn,
                            String status, String conditions, String eligibility, int unpaidDays,
                            Instant lastFreeUse) {
}
