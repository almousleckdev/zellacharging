package com.evcharge.customer.partner;

import com.evcharge.customer.shared.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

public class PartnerFixtures {

    private final JdbcTemplate jdbc;
    private final PartnerKeys keys;

    public PartnerFixtures(JdbcTemplate jdbc) {
        this(jdbc, null);
    }

    public PartnerFixtures(JdbcTemplate jdbc, PartnerKeys keys) {
        this.jdbc = jdbc;
        this.keys = keys;
    }

    public String keyFor(String country) {
        return keys.issue("NDZELA", country, UUID.randomUUID()).key();
    }

    public UUID customer(String country, String firstName, String lastName, String email, String phone,
                         String status) {
        UUID id = UUID.randomUUID();
        TenantContext.runAs(country, () -> jdbc.update("insert into customer (id, first_name, last_name, email, "
                + "phone_e164, password_hash, status, created_at) values (?, ?, ?, ?, ?, 'x', ?, now())", id,
                firstName, lastName, email, phone, status));
        return id;
    }

    public String idTagFor(String country, UUID customer) {
        String tag = "Z-" + customer.toString().replace("-", "").substring(0, 12).toUpperCase();
        TenantContext.runAs(country, () -> jdbc.update("insert into customer_id_tag (customer_id, id_tag, "
                + "created_at) values (?, ?, now())", customer, tag));
        return tag;
    }

    public void expire(UUID keyId) {
        jdbc.update("update partner_api_key set expires_at = ? where id = ?",
                Timestamp.from(Instant.now().minusSeconds(60)), keyId);
    }
}
