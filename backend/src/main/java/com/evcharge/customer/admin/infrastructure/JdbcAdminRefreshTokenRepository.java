package com.evcharge.customer.admin.infrastructure;

import com.evcharge.customer.admin.application.port.AdminRefreshTokenRepository;
import com.evcharge.customer.admin.domain.AdminRefreshToken;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcAdminRefreshTokenRepository implements AdminRefreshTokenRepository {

    private static final RowMapper<AdminRefreshToken> MAPPER = (rs, rowNum) -> new AdminRefreshToken(
            rs.getObject("id", UUID.class),
            rs.getObject("admin_id", UUID.class),
            rs.getObject("family_id", UUID.class),
            rs.getString("token_hash"),
            Timestamps.read(rs, "expires_at"),
            Timestamps.read(rs, "used_at"),
            Timestamps.read(rs, "revoked_at"));

    private final JdbcClient jdbc;

    JdbcAdminRefreshTokenRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(AdminRefreshToken token) {
        jdbc.sql("insert into admin_refresh_token (id, admin_id, family_id, token_hash, expires_at) "
                        + "values (:id, :adminId, :familyId, :hash, :expiresAt)")
                .param("id", token.id()).param("adminId", token.adminId()).param("familyId", token.familyId())
                .param("hash", token.tokenHash()).param("expiresAt", Timestamps.toDb(token.expiresAt())).update();
    }

    @Override
    public Optional<AdminRefreshToken> findByHash(String tokenHash) {
        return jdbc.sql("select * from admin_refresh_token where token_hash = :hash")
                .param("hash", tokenHash).query(MAPPER).optional();
    }

    @Override
    public boolean markUsed(UUID id, Instant at) {
        return jdbc.sql("update admin_refresh_token set used_at = :at where id = :id and used_at is null "
                        + "and revoked_at is null")
                .param("at", Timestamps.toDb(at)).param("id", id).update() == 1;
    }

    @Override
    public void revokeFamily(UUID familyId, Instant at) {
        jdbc.sql("update admin_refresh_token set revoked_at = :at where family_id = :family "
                        + "and revoked_at is null")
                .param("at", Timestamps.toDb(at)).param("family", familyId).update();
    }

    @Override
    public void revokeAllOf(UUID adminId, Instant at) {
        jdbc.sql("update admin_refresh_token set revoked_at = :at where admin_id = :admin and revoked_at is null")
                .param("at", Timestamps.toDb(at)).param("admin", adminId).update();
    }
}
