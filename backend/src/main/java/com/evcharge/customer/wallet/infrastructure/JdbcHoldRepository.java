package com.evcharge.customer.wallet.infrastructure;

import com.evcharge.customer.shared.Timestamps;
import com.evcharge.customer.wallet.application.port.HoldRepository;
import com.evcharge.customer.wallet.domain.FundingHold;
import com.evcharge.customer.wallet.domain.HoldState;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcHoldRepository implements HoldRepository {

    private static final RowMapper<FundingHold> MAPPER = (rs, rowNum) -> new FundingHold(
            rs.getObject("session_id", UUID.class), rs.getObject("customer_id", UUID.class),
            rs.getLong("reserved_minor"), HoldState.valueOf(rs.getString("state")), rs.getBoolean("waived"));

    private final JdbcClient jdbc;

    JdbcHoldRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void insert(FundingHold hold) {
        jdbc.sql("insert into funding_hold (session_id, customer_id, reserved_minor, state, waived, created_at) "
                        + "values (:session, :customer, :reserved, :state, :waived, :now)")
                .param("session", hold.sessionId()).param("customer", hold.customerId())
                .param("reserved", hold.reservedMinor()).param("state", hold.state().name())
                .param("waived", hold.waived())
                .param("now", Timestamps.toDb(Instant.now())).update();
    }

    @Override
    public Optional<FundingHold> lock(UUID sessionId) {
        return jdbc.sql("select * from funding_hold where session_id = :id for update")
                .param("id", sessionId).query(MAPPER).optional();
    }

    @Override
    public Optional<FundingHold> find(UUID sessionId) {
        return jdbc.sql("select * from funding_hold where session_id = :id")
                .param("id", sessionId).query(MAPPER).optional();
    }

    @Override
    public void close(UUID sessionId, HoldState state, long capturedMinor) {
        jdbc.sql("update funding_hold set state = :state, captured_minor = :captured, closed_at = :now "
                        + "where session_id = :id")
                .param("state", state.name()).param("captured", capturedMinor)
                .param("now", Timestamps.toDb(Instant.now())).param("id", sessionId).update();
    }

    @Override
    public void delete(UUID sessionId) {
        jdbc.sql("delete from funding_hold where session_id = :id").param("id", sessionId).update();
    }

    @Override
    public Optional<Long> captured(UUID sessionId) {
        return jdbc.sql("select captured_minor from funding_hold where session_id = :id and captured_minor is not null")
                .param("id", sessionId).query(Long.class).optional();
    }

    @Override
    public List<UUID> openSessionIds() {
        return jdbc.sql("select session_id from funding_hold where state = 'OPEN' order by created_at")
                .query(UUID.class).list();
    }
}
