package com.evcharge.customer.partner;

import com.evcharge.customer.shared.FreeSundayWindows;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface FreeSundayClaims extends FreeSundayWindows {

    boolean claim(UUID customerId, LocalDate sundayDate, UUID sessionId, UUID agreementId, Instant windowEndsAt);

    boolean isClaimed(UUID agreementId, UUID customerId, LocalDate sundayDate);

    void release(UUID sessionId);

    Optional<UUID> agreementOf(UUID sessionId);
}
