package com.evcharge.customer.wallet.application.port;

import com.evcharge.customer.wallet.domain.Withdrawal;
import com.evcharge.customer.wallet.domain.WithdrawalState;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WithdrawalRepository {

    Optional<Withdrawal> insertIfNew(Withdrawal withdrawal);

    Optional<Withdrawal> find(UUID id);

    Optional<Withdrawal> findByIdempotencyKey(UUID customerId, String key);

    List<Withdrawal> recent(UUID customerId, int limit);

    List<Withdrawal> findUnsettled(Instant createdBefore);

    long requestedSince(UUID customerId, Instant since);

    boolean review(UUID id, WithdrawalState from, WithdrawalState to, UUID adminId, String note,
                   String failureCode, Instant now);

    boolean transition(UUID id, WithdrawalState from, WithdrawalState to, String providerRef, String failureCode);
}
