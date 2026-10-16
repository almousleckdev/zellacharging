package com.evcharge.customer.wallet.application.port;

import com.evcharge.customer.wallet.domain.Topup;
import com.evcharge.customer.wallet.domain.TopupState;

import java.util.Optional;
import java.util.UUID;

public interface TopupRepository {

    Optional<Topup> insertIfNew(Topup topup);

    Optional<Topup> find(UUID id);

    Optional<Topup> findByIdempotencyKey(UUID customerId, String key);

    Optional<Topup> findByProviderRef(String providerRef);

    boolean transition(UUID id, TopupState from, TopupState to, String providerRef, String failureCode);
}
