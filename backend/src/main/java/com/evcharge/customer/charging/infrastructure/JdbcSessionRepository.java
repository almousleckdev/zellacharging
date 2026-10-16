package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.shared.Timestamps;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.ApiException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcSessionRepository implements SessionRepository {

    private static final String LIVE = "('REQUESTED','START_ACCEPTED','ACTIVE','STOPPING')";

    private static final RowMapper<ChargingSession> MAPPER = (rs, rowNum) -> new ChargingSession(
            rs.getObject("id", UUID.class),
            rs.getObject("customer_id", UUID.class),
            rs.getString("station_id"),
            rs.getInt("connector_id"),
            rs.getString("id_tag"),
            SessionState.valueOf(rs.getString("state")),
            rs.getString("failure_code"),
            rs.getObject("transaction_id", Integer.class),
            Timestamps.read(rs, "started_at"),
            Timestamps.read(rs, "stopped_at"),
            rs.getBigDecimal("energy_kwh"),
            rs.getObject("cost_minor", Long.class),
            rs.getString("currency"),
            rs.getObject("currency_decimals", Integer.class),
            rs.getString("stop_reason"),
            Timestamps.read(rs, "created_at"),
            Timestamps.read(rs, "updated_at"));

    private final JdbcClient jdbc;

    JdbcSessionRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void insert(ChargingSession s) {
        try {
            jdbc.sql("""
                    insert into charging_session (id, customer_id, station_id, connector_id, id_tag, state,
                                                  created_at, updated_at)
                    values (:id, :customerId, :stationId, :connectorId, :idTag, :state, :createdAt, :updatedAt)
                    """)
                    .param("id", s.id()).param("customerId", s.customerId()).param("stationId", s.stationId())
                    .param("connectorId", s.connectorId()).param("idTag", s.idTag())
                    .param("state", s.state().name()).param("createdAt", Timestamps.toDb(s.createdAt()))
                    .param("updatedAt", Timestamps.toDb(s.updatedAt())).update();
        } catch (DuplicateKeyException e) {
            throw new ApiException(HttpStatus.CONFLICT, "CONNECTOR_OR_SESSION_BUSY",
                    "A charge is already in progress for you or on this connector");
        }
    }

    @Override
    public void update(ChargingSession s) {
        jdbc.sql("""
                update charging_session set state = :state, failure_code = :failure, transaction_id = :txn,
                    started_at = :startedAt, stopped_at = :stoppedAt, energy_kwh = :energy, cost_minor = :cost,
                    currency = :currency, currency_decimals = :decimals, stop_reason = :reason,
                    updated_at = :updatedAt
                where id = :id
                """)
                .param("state", s.state().name()).param("failure", s.failureCode())
                .param("txn", s.transactionId()).param("startedAt", Timestamps.toDb(s.startedAt()))
                .param("stoppedAt", Timestamps.toDb(s.stoppedAt())).param("energy", s.energyKwh())
                .param("cost", s.costMinor()).param("currency", s.currency())
                .param("decimals", s.currencyDecimals()).param("reason", s.stopReason())
                .param("updatedAt", Timestamps.toDb(s.updatedAt())).param("id", s.id()).update();
    }

    @Override
    public Optional<ChargingSession> findOwned(UUID id, UUID customerId) {
        return jdbc.sql("select * from charging_session where id = :id and customer_id = :customerId")
                .param("id", id).param("customerId", customerId).query(MAPPER).optional();
    }

    @Override
    public Optional<ChargingSession> findById(UUID id) {
        return jdbc.sql("select * from charging_session where id = :id").param("id", id).query(MAPPER).optional();
    }

    @Override
    public Optional<ChargingSession> findLive(UUID customerId) {
        return jdbc.sql("select * from charging_session where customer_id = :customerId and state in " + LIVE)
                .param("customerId", customerId).query(MAPPER).optional();
    }

    @Override
    public List<ChargingSession> history(UUID customerId, int limit) {
        return jdbc.sql("select * from charging_session where customer_id = :customerId "
                        + "order by created_at desc limit :limit")
                .param("customerId", customerId).param("limit", limit).query(MAPPER).list();
    }
}
