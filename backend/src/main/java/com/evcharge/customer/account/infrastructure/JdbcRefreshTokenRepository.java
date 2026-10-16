package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.shared.Timestamps;
import com.evcharge.customer.account.application.port.RefreshTokenRepository;
import com.evcharge.customer.account.domain.RefreshToken;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcRefreshTokenRepository implements RefreshTokenRepository {

    private static final RowMapper<RefreshToken> MAPPER = (rs, rowNum) -> new RefreshToken(
            rs.getObject("id", UUID.class),
            rs.getObject("customer_id", UUID.class),
            rs.getObject("family_id", UUID.class),
            rs.getString("token_hash"),
            Timestamps.read(rs, "expires_at"),
            Timestamps.read(rs, "used_at"),
            Timestamps.read(rs, "revoked_at"));

    private final JdbcClient jdbc;

    JdbcRefreshTokenRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(RefreshToken token) {
        jdbc.sql("insert into refresh_token (id, customer_id, family_id, token_hash, expires_at) "
                        + "values (:id, :customerId, :familyId, :hash, :expiresAt)")
                .param("id", token.id()).param("customerId", token.customerId())
                .param("familyId", token.familyId()).param("hash", token.tokenHash())
                .param("expiresAt", Timestamps.toDb(token.expiresAt())).update();
    }

    @Override
    public Optional<RefreshToken> findByHash(String tokenHash) {
        return jdbc.sql("select * from refresh_token where token_hash = :hash")
                .param("hash", tokenHash).query(MAPPER).optional();
    }

    @Override
    public boolean markUsed(UUID id, Instant at) {
        return jdbc.sql("update refresh_token set used_at = :at where id = :id and used_at is null "
                        + "and revoked_at is null")
                .param("at", Timestamps.toDb(at)).param("id", id).update() == 1;
    }

    @Override
    public void revokeFamily(UUID familyId, Instant at) {
        jdbc.sql("update refresh_token set revoked_at = :at where family_id = :family and revoked_at is null")
                .param("at", Timestamps.toDb(at)).param("family", familyId).update();
    }
}
