package com.evcharge.customer.wallet.infrastructure;

import com.evcharge.customer.shared.LogFilter;
import com.evcharge.customer.shared.Timestamps;
import com.evcharge.customer.wallet.LedgerLog;
import com.evcharge.customer.wallet.LedgerPage;
import com.evcharge.customer.wallet.LedgerRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
class JdbcLedgerLog implements LedgerLog {

    private static final String SCOPE = " from ledger_entry where customer_id = :customer "
            + "and country_code = current_setting('app.country', true) "
            + "and type in ('TOPUP', 'WITHDRAWAL', 'WITHDRAWAL_REVERSAL')";

    private static final RowMapper<LedgerRecord> MAPPER = (rs, rowNum) -> new LedgerRecord(
            rs.getObject("id", UUID.class), rs.getString("type"), rs.getLong("amount_minor"),
            rs.getObject("session_id", UUID.class), rs.getObject("topup_id", UUID.class),
            Timestamps.read(rs, "created_at"));

    private final JdbcClient jdbc;

    JdbcLedgerLog(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public LedgerPage search(UUID customerId, LogFilter filter, int limit, int offset) {
        String where = SCOPE + filter.clause("created_at", "customer_id");
        long total = filter.bind(jdbc.sql("select count(*)" + where)).param("customer", customerId)
                .query(Long.class).single();
        var items = filter.bind(jdbc.sql("select *" + where + " order by created_at desc, id limit :limit offset :offset"))
                .param("customer", customerId).param("limit", limit).param("offset", offset).query(MAPPER).list();
        var currency = jdbc.sql("select currency, decimals from wallet where customer_id = :customer")
                .param("customer", customerId)
                .query((rs, rowNum) -> new String[]{rs.getString(1).trim(), rs.getString(2)}).optional();
        return new LedgerPage(total, currency.map(row -> row[0]).orElse(null),
                currency.map(row -> Integer.parseInt(row[1])).orElse(0), items);
    }
}
