package com.evcharge.customer.account.application.port;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PendingPhoneRepository {

    void save(UUID customerId, String phoneE164, Instant at);

    Optional<String> find(UUID customerId);

    void delete(UUID customerId);
}
