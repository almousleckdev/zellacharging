package com.evcharge.customer.partner;

import com.evcharge.customer.partner.domain.EvidenceReference;
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

    public void suspend(String country, UUID customer, long owedMinor, String... numbers) {
        TenantContext.runAs(country, () -> {
            jdbc.update("insert into partner_link (partner, customer_id, linked_at) values ('NDZELA', ?, now()) "
                    + "on conflict do nothing", customer);
            jdbc.update("insert into partner_suspension (partner, customer_id, owed_minor, currency, decimals, "
                    + "blocked_before, suspended_at) values ('NDZELA', ?, ?, 'XAF', 0, false, now())",
                    customer, owedMinor);
            for (String number : numbers) {
                jdbc.update("insert into suspended_number (partner, customer_id, msisdn) "
                        + "values ('NDZELA', ?, ?)", customer, number);
            }
        });
        jdbc.update("update customer set status = 'BLOCKED' where id = ?", customer);
    }

    public void evidence(String country, UUID suspended, UUID receiving, boolean receivingLinked, long amountMinor,
                         String key) {
        TenantContext.runAs(country, () -> jdbc.update("insert into suspension_evidence (id, partner, "
                + "suspended_customer_id, receiving_customer_id, receiving_ref, receiving_linked, kind, msisdn, "
                + "amount_minor, currency, idempotency_key, occurred_at) values (?, 'NDZELA', ?, ?, ?, ?, "
                + "'TOPUP_FROM_SUSPENDED_NUMBER', '+24177000000', ?, 'XAF', ?, now())", UUID.randomUUID(), suspended,
                receiving, EvidenceReference.of(receiving), receivingLinked, amountMinor, key));
    }

    public void link(String country, UUID customer) {
        TenantContext.runAs(country, () -> jdbc.update("insert into partner_link (partner, customer_id, "
                + "linked_at) values ('NDZELA', ?, now()) on conflict do nothing", customer));
    }

    public String idTagFor(String country, UUID customer) {
        String tag = "Z-" + customer.toString().replace("-", "").substring(0, 12).toUpperCase();
        TenantContext.runAs(country, () -> jdbc.update("insert into customer_id_tag (customer_id, id_tag, "
                + "created_at) values (?, ?, now())", customer, tag));
        return tag;
    }

    public UUID session(UUID customer, String state, Integer transactionId, Instant at) {
        UUID id = UUID.randomUUID();
        Timestamp when = Timestamp.from(at);
        jdbc.update("insert into charging_session (id, customer_id, station_id, connector_id, id_tag, state, "
                + "transaction_id, started_at, energy_kwh, currency, currency_decimals, created_at, updated_at) "
                + "values (?, ?, ?, 1, 'Z-TAG', ?, ?, ?, 12.5, 'XOF', 0, ?, ?)", id, customer,
                "CP-" + id.toString().substring(0, 8), state, transactionId, when, when, when);
        return id;
    }

    public void expire(UUID keyId) {
        jdbc.update("update partner_api_key set expires_at = ? where id = ?",
                Timestamp.from(Instant.now().minusSeconds(60)), keyId);
    }
}
