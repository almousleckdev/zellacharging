package com.evcharge.customer.account.application.port;

import com.evcharge.customer.account.domain.Channel;
import com.evcharge.customer.account.domain.VerificationCode;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface VerificationCodeRepository {

    void save(VerificationCode code);

    Optional<VerificationCode> findLatest(UUID customerId, Channel channel);

    void incrementAttempts(UUID id);

    void consume(UUID id, Instant at);
}
