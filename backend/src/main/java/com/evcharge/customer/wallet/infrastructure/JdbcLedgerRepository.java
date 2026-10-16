package com.evcharge.customer.wallet.infrastructure;

import com.evcharge.customer.shared.Timestamps;
import com.evcharge.customer.wallet.application.port.LedgerRepository;
import com.evcharge.customer.wallet.domain.LedgerEntry;
import com.evcharge.customer.wallet.domain.LedgerType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
class JdbcLedgerRepository implements LedgerRepository {

    private static final RowMapper<LedgerEntry> MAPPER = (rs, rowNum) -> new LedgerEntry(
            rs.getObject("id", UUID.class), rs.getObject("customer_id", UUID.class),
            LedgerType.valueOf(rs.getString("type")), rs.getLong("amount_minor"),
            rs.getObject("session_id", UUID.class), rs.getObject("topup_id", UUID.class),
            rs.getString("idempotency_key"), Timestamps.read(rs, "created_at"));

    private final JdbcClient jdbc;

    JdbcLedgerRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean append(LedgerEntry entry) {
        return jdbc.sql("insert into ledger_entry (id, customer_id, type, amount_minor, session_id, topup_id, "
                        + "idempotency_key, created_at) values (:id, :customer, :type, :amount, :session, :topup, "
                        + ":key, :at) on conflict (idempotency_key) do nothing")
                .param("id", entry.id()).param("customer", entry.customerId()).param("type", entry.type().name())
                .param("amount", entry.amountMinor()).param("session", entry.sessionId())
                .param("topup", entry.topupId()).param("key", entry.idempotencyKey())
                .param("at", Timestamps.toDb(entry.createdAt())).update() == 1;
    }

    @Override
    public List<LedgerEntry> recent(UUID customerId, int limit) {
        return jdbc.sql("select * from ledger_entry where customer_id = :id "
                        + "order by created_at desc limit :limit")
                .param("id", customerId).param("limit", limit).query(MAPPER).list();
    }
}
