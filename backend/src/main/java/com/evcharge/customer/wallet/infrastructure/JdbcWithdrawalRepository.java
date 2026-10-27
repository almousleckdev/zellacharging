package com.evcharge.customer.wallet.infrastructure;

import com.evcharge.customer.shared.Timestamps;
import com.evcharge.customer.wallet.application.port.WithdrawalRepository;
import com.evcharge.customer.wallet.domain.MobileOperator;
import com.evcharge.customer.wallet.domain.Withdrawal;
import com.evcharge.customer.wallet.domain.WithdrawalState;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcWithdrawalRepository implements WithdrawalRepository {

    private static final RowMapper<Withdrawal> MAPPER = (rs, rowNum) -> new Withdrawal(
            rs.getObject("id", UUID.class), rs.getObject("customer_id", UUID.class),
            MobileOperator.valueOf(rs.getString("operator")), rs.getString("msisdn"), rs.getLong("amount_minor"),
            WithdrawalState.valueOf(rs.getString("state")), rs.getString("reference"),
            rs.getString("provider_ref"), rs.getString("failure_code"), rs.getString("idempotency_key"),
            Timestamps.read(rs, "created_at"), Timestamps.read(rs, "updated_at"));

    private final JdbcClient jdbc;

    JdbcWithdrawalRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Withdrawal> insertIfNew(Withdrawal w) {
        int inserted = jdbc.sql("insert into withdrawal (id, customer_id, operator, msisdn, amount_minor, state, "
                        + "reference, idempotency_key, created_at, updated_at) values (:id, :customer, :operator, "
                        + ":msisdn, :amount, :state, :reference, :key, :created, :updated) on conflict do nothing")
                .param("id", w.id()).param("customer", w.customerId()).param("operator", w.operator().name())
                .param("msisdn", w.msisdn()).param("amount", w.amountMinor()).param("state", w.state().name())
                .param("reference", w.reference()).param("key", w.idempotencyKey())
                .param("created", Timestamps.toDb(w.createdAt())).param("updated", Timestamps.toDb(w.updatedAt()))
                .update();
        return inserted == 1 ? Optional.of(w) : Optional.empty();
    }

    @Override
    public Optional<Withdrawal> find(UUID id) {
        return jdbc.sql("select * from withdrawal where id = :id").param("id", id).query(MAPPER).optional();
    }

    @Override
    public Optional<Withdrawal> findByIdempotencyKey(UUID customerId, String key) {
        return jdbc.sql("select * from withdrawal where customer_id = :customer and idempotency_key = :key")
                .param("customer", customerId).param("key", key).query(MAPPER).optional();
    }

    @Override
    public List<Withdrawal> recent(UUID customerId, int limit) {
        return jdbc.sql("select * from withdrawal where customer_id = :customer order by created_at desc limit :limit")
                .param("customer", customerId).param("limit", limit).query(MAPPER).list();
    }

    @Override
    public List<Withdrawal> findUnsettled(Instant createdBefore) {
        return jdbc.sql("select * from withdrawal where state in ('REQUESTED', 'PROCESSING') "
                        + "and created_at < :before order by created_at limit 200")
                .param("before", Timestamps.toDb(createdBefore)).query(MAPPER).list();
    }

    @Override
    public long requestedSince(UUID customerId, Instant since) {
        return jdbc.sql("select coalesce(sum(amount_minor), 0) from withdrawal where customer_id = :customer "
                        + "and state <> 'FAILED' and created_at > :since")
                .param("customer", customerId).param("since", Timestamps.toDb(since)).query(Long.class).single();
    }

    @Override
    public boolean transition(UUID id, WithdrawalState from, WithdrawalState to, String providerRef,
                              String failureCode) {
        return jdbc.sql("update withdrawal set state = :to, provider_ref = coalesce(:ref, provider_ref), "
                        + "failure_code = :failure, updated_at = :now where id = :id and state = :from")
                .param("to", to.name()).param("ref", providerRef).param("failure", failureCode)
                .param("now", Timestamps.toDb(Instant.now())).param("id", id).param("from", from.name())
                .update() == 1;
    }
}
