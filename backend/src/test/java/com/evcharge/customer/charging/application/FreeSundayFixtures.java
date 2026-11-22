package com.evcharge.customer.charging.application;

import com.evcharge.customer.FakeOcppServer;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.partner.application.port.FreeChargingEligibility;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

final class FreeSundayFixtures {

    static final LocalDate SUNDAY = LocalDate.of(2026, 11, 22);

    private static final AtomicInteger SEQUENCE = new AtomicInteger();
    private static final AtomicInteger TRANSACTIONS = new AtomicInteger(800000);

    private final JdbcTemplate jdbc;
    private final FakeOcppServer ocpp;

    FreeSundayFixtures(JdbcTemplate jdbc, FakeOcppServer ocpp) {
        this.jdbc = jdbc;
        this.ocpp = ocpp;
    }

    static FreeChargingEligibility eligible() {
        return new FreeChargingEligibility(true, "ELIGIBLE", UUID.randomUUID(), SUNDAY,
                Instant.now().plus(2, ChronoUnit.HOURS));
    }

    static ChargingSession finished(ChargingSession session, SessionState state, BigDecimal kwh, long costMinor) {
        Instant now = Instant.now();
        return started(session).withMeasuredEnergy(kwh).withCost(costMinor, "XOF", 0, now).withState(state, null, now);
    }

    static ChargingSession started(ChargingSession session) {
        Instant now = Instant.now();
        return new ChargingSession(session.id(), session.customerId(), session.stationId(), session.connectorId(),
                session.idTag(), SessionState.ACTIVE, null, TRANSACTIONS.incrementAndGet(),
                now.minus(1, ChronoUnit.HOURS), null, null, null, null, null, null, session.createdAt(), now);
    }

    UUID customerWith(long balance) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, phone_e164, email_verified_at, "
                + "phone_verified_at, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, ?, now(), now(), 'x', 'ACTIVE', now())", id, id + "@example.com",
                "+24166" + String.format("%07d", Math.abs(id.hashCode()) % 10000000));
        jdbc.update("insert into wallet (customer_id, currency, decimals, balance_minor, created_at) "
                + "values (?, 'XOF', 0, ?, now())", id, balance);
        return id;
    }

    UUID linkedCustomerWith(long balance) {
        UUID id = customerWith(balance);
        jdbc.update("insert into partner_link (partner, customer_id, linked_at) values ('NDZELA', ?, now())", id);
        return id;
    }

    String station() {
        return station(200, "{\"status\":\"PENDING\"}");
    }

    String rejectingStation() {
        return station(409, "{\"status\":\"REJECTED\",\"reasonCode\":\"CHARGER_REFUSED\"}");
    }

    private String station(int status, String reply) {
        String id = "CP-SUN" + SEQUENCE.incrementAndGet();
        ocpp.station(id).route("POST", "/api/v1/charge-points/" + id + "/connectors/1/start", status, reply);
        return id;
    }

    List<String> claimStates(UUID customer) {
        return jdbc.queryForList("select state from free_sunday_claim where customer_id = ? order by claimed_at",
                String.class, customer);
    }

    String claimStateOf(UUID session) {
        return jdbc.queryForObject("select state from free_sunday_claim where session_id = ?", String.class, session);
    }

    long reserved(UUID customer) {
        return jdbc.queryForObject("select reserved_minor from wallet where customer_id = ?", Long.class, customer);
    }

    long balance(UUID customer) {
        return jdbc.queryForObject("select balance_minor from wallet where customer_id = ?", Long.class, customer);
    }

    boolean waived(UUID session) {
        return jdbc.queryForObject("select waived from funding_hold where session_id = ?", Boolean.class, session);
    }

    String holdState(UUID session) {
        return jdbc.queryForObject("select state from funding_hold where session_id = ?", String.class, session);
    }

    int ledgerEntries(UUID session) {
        return jdbc.queryForObject("select count(*) from ledger_entry where session_id = ?", Integer.class, session);
    }

    List<UUID> liveSessions(UUID customer) {
        return jdbc.queryForList("select id from charging_session where customer_id = ? and state in "
                + "('REQUESTED', 'START_ACCEPTED', 'ACTIVE', 'STOPPING')", UUID.class, customer);
    }

    int waivedHolds(UUID customer) {
        return jdbc.queryForObject("select count(*) from funding_hold where customer_id = ? and waived",
                Integer.class, customer);
    }
}
