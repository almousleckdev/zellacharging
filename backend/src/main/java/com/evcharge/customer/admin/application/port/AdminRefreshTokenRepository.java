package com.evcharge.customer.admin.application.port;

import com.evcharge.customer.admin.domain.AdminRefreshToken;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AdminRefreshTokenRepository {

    void save(AdminRefreshToken token);
    Optional<AdminRefreshToken> findByHash(String tokenHash);
    boolean markUsed(UUID id, Instant at);
    void revokeFamily(UUID familyId, Instant at);
    void revokeAllOf(UUID adminId, Instant at);
}
