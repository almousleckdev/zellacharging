package com.evcharge.customer.wallet.infrastructure;

import com.evcharge.customer.shared.Timestamps;
import com.evcharge.customer.wallet.application.port.LedgerRepository;
import com.evcharge.customer.wallet.domain.LedgerDiscrepancy;
import com.evcharge.customer.wallet.domain.LedgerEntry;
import com.evcharge.customer.wallet.domain.LedgerType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
    public List<LedgerDiscrepancy> discrepancies() {
        return jdbc.sql("""
                select w.customer_id, w.balance_minor, w.reserved_minor,
                    coalesce(sum(case l.type when 'TOPUP' then l.amount_minor
                        when 'WITHDRAWAL_REVERSAL' then l.amount_minor when 'CAPTURE' then -l.amount_minor
                        when 'WITHDRAWAL' then -l.amount_minor else 0 end), 0) as expected_balance,
                    coalesce(sum(case l.type when 'RESERVE' then l.amount_minor
                        when 'RELEASE' then -l.amount_minor when 'CAPTURE' then -l.amount_minor else 0 end), 0)
                        as expected_reserved
                from wallet w left join ledger_entry l on l.customer_id = w.customer_id
                group by w.customer_id, w.balance_minor, w.reserved_minor
                having w.balance_minor <> coalesce(sum(case l.type when 'TOPUP' then l.amount_minor
                        when 'WITHDRAWAL_REVERSAL' then l.amount_minor when 'CAPTURE' then -l.amount_minor
                        when 'WITHDRAWAL' then -l.amount_minor else 0 end), 0)
                    or w.reserved_minor <> coalesce(sum(case l.type when 'RESERVE' then l.amount_minor
                        when 'RELEASE' then -l.amount_minor when 'CAPTURE' then -l.amount_minor else 0 end), 0)
                """)
                .query((rs, rowNum) -> new LedgerDiscrepancy(rs.getObject("customer_id", UUID.class),
                        rs.getLong("balance_minor"), rs.getLong("expected_balance"), rs.getLong("reserved_minor"),
                        rs.getLong("expected_reserved"))).list();
    }

    @Override
    public Optional<LedgerEntry> find(UUID customerId, UUID id) {
        return jdbc.sql("select l.* from ledger_entry l where l.customer_id = :customer and l.id = :id and not exists "
                        + "(select 1 from ledger_hidden h where h.entry_id = l.id)")
                .param("customer", customerId).param("id", id).query(MAPPER).optional();
    }

    @Override
    public long balanceAfter(UUID customerId, Instant at) {
        return jdbc.sql("""
                select coalesce(sum(case type when 'TOPUP' then amount_minor
                    when 'WITHDRAWAL_REVERSAL' then amount_minor when 'CAPTURE' then -amount_minor
                    when 'WITHDRAWAL' then -amount_minor else 0 end), 0)
                from ledger_entry where customer_id = :customer and created_at <= :at
                """).param("customer", customerId).param("at", Timestamps.toDb(at)).query(Long.class).single();
    }

    @Override
    public boolean hide(UUID customerId, UUID id, Instant at) {
        return jdbc.sql("insert into ledger_hidden (entry_id, customer_id, hidden_at) select l.id, l.customer_id, :at "
                        + "from ledger_entry l where l.id = :id and l.customer_id = :customer "
                        + "on conflict (entry_id) do nothing")
                .param("id", id).param("customer", customerId).param("at", Timestamps.toDb(at)).update() == 1;
    }

    @Override
    public List<LedgerEntry> recent(UUID customerId, int limit) {
        return jdbc.sql("select l.* from ledger_entry l where l.customer_id = :id and not exists "
                        + "(select 1 from ledger_hidden h where h.entry_id = l.id) "
                        + "order by l.created_at desc limit :limit")
                .param("id", customerId).param("limit", limit).query(MAPPER).list();
    }
}
