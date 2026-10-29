package com.evcharge.customer.account.application.port;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PendingEmailRepository {

    void save(UUID customerId, String email, Instant at);

    Optional<String> find(UUID customerId);

    void delete(UUID customerId);
}
