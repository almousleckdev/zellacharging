package com.evcharge.customer.partner.infrastructure;

import com.evcharge.customer.partner.application.port.PartnerKeyStore;
import com.evcharge.customer.partner.domain.PartnerKey;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcPartnerKeyStore implements PartnerKeyStore {

    private static final RowMapper<PartnerKey> MAPPER = (rs, row) -> new PartnerKey(
            rs.getObject("id", UUID.class), rs.getString("partner"), rs.getString("country_code"),
            rs.getString("key_hash"), Timestamps.read(rs, "created_at"), Timestamps.read(rs, "expires_at"),
            Timestamps.read(rs, "revoked_at"), Timestamps.read(rs, "last_used_at"));

    private final JdbcClient jdbc;

    JdbcPartnerKeyStore(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void insert(PartnerKey key) {
        jdbc.sql("insert into partner_api_key (id, partner, country_code, key_hash, created_at, expires_at) "
                        + "values (:id, :partner, :country, :hash, :created, :expires)")
                .param("id", key.id()).param("partner", key.partner()).param("country", key.country())
                .param("hash", key.keyHash()).param("created", Timestamps.toDb(key.createdAt()))
                .param("expires", Timestamps.toDb(key.expiresAt())).update();
    }

    @Override
    public Optional<PartnerKey> findByHash(String keyHash) {
        return jdbc.sql("select * from partner_api_key where key_hash = :hash").param("hash", keyHash)
                .query(MAPPER).optional();
    }

    @Override
    public Optional<PartnerKey> findById(UUID id) {
        return jdbc.sql("select * from partner_api_key where id = :id").param("id", id).query(MAPPER).optional();
    }

    @Override
    public void revoke(UUID id, Instant at) {
        jdbc.sql("update partner_api_key set revoked_at = :at where id = :id and revoked_at is null")
                .param("at", Timestamps.toDb(at)).param("id", id).update();
    }

    @Override
    public void touch(UUID id, Instant at) {
        jdbc.sql("update partner_api_key set last_used_at = :at where id = :id")
                .param("at", Timestamps.toDb(at)).param("id", id).update();
    }

    @Override
    public List<PartnerKey> list(String partner) {
        return jdbc.sql("select * from partner_api_key where partner = :partner order by created_at desc")
                .param("partner", partner).query(MAPPER).list();
    }
}
