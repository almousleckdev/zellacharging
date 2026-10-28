package com.evcharge.customer.wallet.application.port;

import com.evcharge.customer.wallet.domain.MobileOperator;
import com.evcharge.customer.wallet.domain.Topup;
import com.evcharge.customer.wallet.domain.TopupState;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TopupRepository {

    Optional<Topup> insertIfNew(Topup topup);

    Optional<Topup> find(UUID id);

    Optional<Topup> findByIdempotencyKey(UUID customerId, String key);

    Optional<Topup> findByReference(String reference);

    Optional<Topup> lastSucceeded(UUID customerId, MobileOperator operator);

    Optional<Topup> lastSucceeded(UUID customerId);

    boolean hasPending(UUID customerId, Instant createdAfter);

    List<Topup> findUnsettled(Instant createdAfter, Instant createdBefore);

    boolean transition(UUID id, TopupState from, TopupState to, String providerRef, String failureCode);
}
