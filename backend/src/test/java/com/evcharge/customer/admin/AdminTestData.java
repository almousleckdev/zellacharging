package com.evcharge.customer.admin;

import com.evcharge.customer.shared.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.UUID;

public class AdminTestData {

    private static final String NEXT_TRANSACTION = "(select coalesce(max(transaction_id), 0) + 1 from charging_session)";

    private final JdbcTemplate jdbc;

    public AdminTestData(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public UUID customer(String country, String status) {
        return customer(country, status, "A", "B", null, Instant.now());
    }

    public UUID customer(String country, String status, String first, String last, String phone, Instant createdAt) {
        UUID id = UUID.randomUUID();
        TenantContext.runAs(country, () -> jdbc.update("insert into customer (id, first_name, last_name, email, "
                + "phone_e164, password_hash, status, created_at) values (?, ?, ?, ?, ?, 'x', ?, ?)", id, first,
                last, id + "@example.com", phone, status, java.sql.Timestamp.from(createdAt)));
        return id;
    }

    public void wallet(String country, UUID customer, String currency, long balance, long reserved) {
        TenantContext.runAs(country, () -> jdbc.update("insert into wallet (customer_id, currency, decimals, "
                + "balance_minor, reserved_minor, created_at) values (?, ?, 0, ?, ?, now())", customer, currency,
                balance, reserved));
    }

    public void session(String country, UUID customer, int connector, String state, String energy, Long cost) {
        TenantContext.runAs(country, () -> jdbc.update("insert into charging_session (id, customer_id, station_id, "
                + "connector_id, id_tag, state, transaction_id, started_at, energy_kwh, cost_minor, created_at, "
                + "updated_at) values (?, ?, ?, ?, 'Z-x', ?, " + NEXT_TRANSACTION + ", now(), ?::numeric, ?, now(), now())", UUID.randomUUID(), customer,
                "CP-" + UUID.randomUUID(), connector, state, energy, cost));
    }

    public void complimentarySession(String country, UUID customer, String energy, long cost) {
        UUID id = UUID.randomUUID();
        TenantContext.runAs(country, () -> {
            jdbc.update("insert into charging_session (id, customer_id, station_id, connector_id, id_tag, state, "
                    + "transaction_id, started_at, energy_kwh, cost_minor, created_at, updated_at) values (?, ?, ?, 1, "
                    + "'Z-x', 'COMPLETED', " + NEXT_TRANSACTION + ", now(), ?::numeric, ?, now(), now())", id, customer, "CP-" + UUID.randomUUID(),
                    energy, cost);
            jdbc.update("insert into funding_hold (session_id, customer_id, reserved_minor, state, waived, "
                    + "created_at) values (?, ?, ?, 'SETTLED', true, now())", id, customer, cost);
        });
    }

    public void sessionAt(String country, UUID customer, String station, int connector, String state, String energy,
                          Long cost) {
        TenantContext.runAs(country, () -> jdbc.update("insert into charging_session (id, customer_id, station_id, "
                + "connector_id, id_tag, state, transaction_id, started_at, energy_kwh, cost_minor, created_at, "
                + "updated_at) values (?, ?, ?, ?, 'Z-x', ?, " + NEXT_TRANSACTION + ", now(), ?::numeric, ?, now(), now())", UUID.randomUUID(), customer,
                station, connector, state, energy, cost));
    }

    public void topup(String country, UUID customer, String state, long amount) {
        TenantContext.runAs(country, () -> jdbc.update("insert into topup (id, customer_id, operator, msisdn, "
                + "amount_minor, state, idempotency_key, reference, created_at, updated_at) "
                + "values (?, ?, 'AIRTEL', '+24177000000', ?, ?, ?, ?, now(), now())", UUID.randomUUID(), customer,
                amount, state, UUID.randomUUID().toString(), UUID.randomUUID().toString()));
    }

    public void withdrawal(String country, UUID customer, String state, long amount) {
        TenantContext.runAs(country, () -> jdbc.update("insert into withdrawal (id, customer_id, operator, msisdn, "
                + "amount_minor, state, reference, idempotency_key, created_at, updated_at) "
                + "values (?, ?, 'MOOV', '+24162000000', ?, ?, ?, ?, now(), now())", UUID.randomUUID(), customer,
                amount, state, UUID.randomUUID().toString(), UUID.randomUUID().toString()));
    }

    public void capture(String country, UUID customer, long amount) {
        TenantContext.runAs(country, () -> jdbc.update("insert into ledger_entry (id, customer_id, type, "
                + "amount_minor, idempotency_key, created_at) values (?, ?, 'CAPTURE', ?, ?, now())",
                UUID.randomUUID(), customer, amount, UUID.randomUUID().toString()));
    }
}
