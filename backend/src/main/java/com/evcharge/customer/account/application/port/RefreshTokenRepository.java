package com.evcharge.customer.account.application.port;

import com.evcharge.customer.account.domain.RefreshToken;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {

    void save(RefreshToken token);

    Optional<RefreshToken> findByHash(String tokenHash);

    boolean markUsed(UUID id, Instant at);

    void revokeFamily(UUID familyId, Instant at);
}
