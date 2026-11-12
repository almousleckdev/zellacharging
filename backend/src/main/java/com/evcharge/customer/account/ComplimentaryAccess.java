package com.evcharge.customer.account;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ComplimentaryAccess {

    void grant(UUID customerId, UUID grantedBy, String reason, Instant expiresAt);

    boolean revoke(UUID customerId);

    Optional<Complimentary> find(UUID customerId);

    List<Complimentary> all();
}
