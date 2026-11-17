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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public void insert(ChargingSession s, String idempotencyKey) {
        try {
            jdbc.sql("""
                    insert into charging_session (id, customer_id, station_id, connector_id, id_tag, state,
                                                  idempotency_key, created_at, updated_at)
                    values (:id, :customerId, :stationId, :connectorId, :idTag, :state, :key, :createdAt, :updatedAt)
                    """)
                    .param("id", s.id()).param("customerId", s.customerId()).param("stationId", s.stationId())
                    .param("connectorId", s.connectorId()).param("idTag", s.idTag())
                    .param("state", s.state().name()).param("key", idempotencyKey).param("createdAt", Timestamps.toDb(s.createdAt()))
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
    public void reassignIdTag(UUID id, String idTag) {
        jdbc.sql("update charging_session set id_tag = :idTag where id = :id")
                .param("idTag", idTag).param("id", id).update();
    }

    @Override
    public boolean hide(UUID id, UUID customerId, Instant at) {
        return jdbc.sql("update charging_session set hidden_at = :at where id = :id and customer_id = :customer "
                        + "and hidden_at is null and state not in " + LIVE)
                .param("at", Timestamps.toDb(at)).param("id", id).param("customer", customerId).update() > 0;
    }

    @Override
    public Map<String, Set<Integer>> liveConnectors() {
        return jdbc.sql("select station_id, connector_id from charging_session where state in " + LIVE)
                .query((rs, rowNum) -> Map.entry(rs.getString("station_id"), rs.getInt("connector_id"))).list()
                .stream().collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toSet())));
    }

    @Override
    public List<ChargingSession> live() {
        return jdbc.sql("select * from charging_session where state in " + LIVE).query(MAPPER).list();
    }

    @Override
    public List<ChargingSession> closedWithoutCharger(Instant since) {
        return jdbc.sql("select * from charging_session where state = 'ABANDONED' and failure_code = 'CHARGER_OFFLINE' "
                        + "and transaction_id is not null and updated_at >= :since")
                .param("since", java.sql.Timestamp.from(since)).query(MAPPER).list();
    }

    @Override
    public Optional<ChargingSession> findOwned(UUID id, UUID customerId) {
        return jdbc.sql("select * from charging_session where id = :id and customer_id = :customerId and hidden_at is null")
                .param("id", id).param("customerId", customerId).query(MAPPER).optional();
    }

    @Override
    public Optional<ChargingSession> findByIdempotencyKey(UUID customerId, String idempotencyKey) {
        return jdbc.sql("select * from charging_session where customer_id = :customerId "
                        + "and idempotency_key = :key")
                .param("customerId", customerId).param("key", idempotencyKey).query(MAPPER).optional();
    }

    @Override
    public Optional<ChargingSession> findById(UUID id) {
        return jdbc.sql("select * from charging_session where id = :id").param("id", id).query(MAPPER).optional();
    }

    @Override
    public Optional<ChargingSession> findByTransactionId(int transactionId) {
        return jdbc.sql("select * from charging_session where transaction_id = :transactionId "
                        + "order by created_at desc limit 1")
                .param("transactionId", transactionId).query(MAPPER).optional();
    }

    @Override
    public Optional<ChargingSession> findLive(UUID customerId) {
        return jdbc.sql("select * from charging_session where customer_id = :customerId and state in " + LIVE)
                .param("customerId", customerId).query(MAPPER).optional();
    }

    @Override
    public Optional<ChargingSession> findLiveAt(String stationId, int connectorId, String idTag) {
        return jdbc.sql("select * from charging_session where station_id = :station and connector_id = :connector "
                        + "and id_tag = :idTag and state in " + LIVE + " order by created_at desc limit 1")
                .param("station", stationId).param("connector", connectorId).param("idTag", idTag)
                .query(MAPPER).optional();
    }

    @Override
    public List<ChargingSession> history(UUID customerId, int limit) {
        return jdbc.sql("select * from charging_session where customer_id = :customerId and hidden_at is null "
                        + "order by created_at desc limit :limit")
                .param("customerId", customerId).param("limit", limit).query(MAPPER).list();
    }
}
