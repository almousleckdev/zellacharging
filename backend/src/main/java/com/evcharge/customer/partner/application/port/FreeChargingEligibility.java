package com.evcharge.customer.partner.application.port;

import com.evcharge.customer.shared.FreeSundayWindow;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FreeChargingEligibility(boolean eligible, String reason, UUID agreementId, LocalDate sundayDate,
                                      Instant windowEndsAt, FreeSundayWindow window) {

    public static final String UNAVAILABLE = "UNAVAILABLE";

    public FreeChargingEligibility(boolean eligible, String reason, UUID agreementId, LocalDate sundayDate,
                                   Instant windowEndsAt) {
        this(eligible, reason, agreementId, sundayDate, windowEndsAt, null);
    }

    public static FreeChargingEligibility unavailable() {
        return new FreeChargingEligibility(false, UNAVAILABLE, null, null, null);
    }

    public boolean complete() {
        return agreementId != null && sundayDate != null && windowEndsAt != null;
    }
}
