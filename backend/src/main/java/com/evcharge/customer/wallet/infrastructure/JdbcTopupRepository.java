package com.evcharge.customer.wallet.infrastructure;

import com.evcharge.customer.shared.Timestamps;
import com.evcharge.customer.wallet.application.port.TopupRepository;
import com.evcharge.customer.wallet.domain.MobileOperator;
import com.evcharge.customer.wallet.domain.Topup;
import com.evcharge.customer.wallet.domain.TopupState;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcTopupRepository implements TopupRepository {

    private static final RowMapper<Topup> MAPPER = (rs, rowNum) -> new Topup(
            rs.getObject("id", UUID.class), rs.getObject("customer_id", UUID.class),
            MobileOperator.valueOf(rs.getString("operator")), rs.getString("msisdn"),
            rs.getLong("amount_minor"), TopupState.valueOf(rs.getString("state")),
            rs.getString("provider_ref"), rs.getString("failure_code"), rs.getString("idempotency_key"),
            Timestamps.read(rs, "created_at"), Timestamps.read(rs, "updated_at"));

    private final JdbcClient jdbc;

    JdbcTopupRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Topup> insertIfNew(Topup topup) {
        int inserted = jdbc.sql("insert into topup (id, customer_id, operator, msisdn, amount_minor, state, "
                        + "idempotency_key, created_at, updated_at) values (:id, :customer, :operator, :msisdn, "
                        + ":amount, :state, :key, :created, :updated) on conflict do nothing")
                .param("id", topup.id()).param("customer", topup.customerId())
                .param("operator", topup.operator().name()).param("msisdn", topup.msisdn())
                .param("amount", topup.amountMinor()).param("state", topup.state().name())
                .param("key", topup.idempotencyKey()).param("created", Timestamps.toDb(topup.createdAt()))
                .param("updated", Timestamps.toDb(topup.updatedAt())).update();
        return inserted == 1 ? Optional.of(topup) : Optional.empty();
    }

    @Override
    public Optional<Topup> find(UUID id) {
        return jdbc.sql("select * from topup where id = :id").param("id", id).query(MAPPER).optional();
    }

    @Override
    public Optional<Topup> findByIdempotencyKey(UUID customerId, String key) {
        return jdbc.sql("select * from topup where customer_id = :customer and idempotency_key = :key")
                .param("customer", customerId).param("key", key).query(MAPPER).optional();
    }

    @Override
    public Optional<Topup> findByProviderRef(String providerRef) {
        return jdbc.sql("select * from topup where provider_ref = :ref")
                .param("ref", providerRef).query(MAPPER).optional();
    }

    @Override
    public boolean transition(UUID id, TopupState from, TopupState to, String providerRef, String failureCode) {
        return jdbc.sql("update topup set state = :to, provider_ref = coalesce(:ref, provider_ref), "
                        + "failure_code = :failure, updated_at = :now where id = :id and state = :from")
                .param("to", to.name()).param("ref", providerRef).param("failure", failureCode)
                .param("now", Timestamps.toDb(Instant.now())).param("id", id).param("from", from.name())
                .update() == 1;
    }
}
