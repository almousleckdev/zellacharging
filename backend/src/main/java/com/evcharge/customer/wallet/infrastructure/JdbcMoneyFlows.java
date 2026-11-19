package com.evcharge.customer.wallet.infrastructure;

import com.evcharge.customer.shared.Bucket;
import com.evcharge.customer.shared.LocalDays;
import com.evcharge.customer.shared.ReportingWindow;
import com.evcharge.customer.shared.Timestamps;
import com.evcharge.customer.wallet.DailyMoney;
import com.evcharge.customer.wallet.MoneyFlows;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Repository
class JdbcMoneyFlows implements MoneyFlows {

    private static final String SCOPE = " where country_code = current_setting('app.country', true) "
            + "and created_at >= greatest(:from, :since) and created_at < :to";

    private static final String STARTED = "coalesce(s.created_at, l.created_at)";

    private final JdbcClient jdbc;
    private final ReportingWindow reporting;

    JdbcMoneyFlows(JdbcClient jdbc, ReportingWindow reporting) {
        this.jdbc = jdbc;
        this.reporting = reporting;
    }

    @Override
    public List<DailyMoney> between(Instant from, Instant to, Bucket bucket) {
        Map<LocalDate, long[]> days = new TreeMap<>();
        String day = LocalDays.bucket("created_at", bucket) + " as day";
        collect(days, "select " + day + ", coalesce(sum(amount_minor), 0)::bigint, count(*) from topup" + SCOPE
                + " and state = 'SUCCEEDED' and (provider_ref is null or provider_ref not like 'sandbox-%') "
                + "group by 1", from, to, 0, 1);
        collect(days, "select " + day + ", coalesce(sum(amount_minor), 0)::bigint from withdrawal" + SCOPE
                + " and state = 'PAID' group by 1", from, to, 2);
        collect(days, "select " + LocalDays.bucket(STARTED, bucket) + " as day, coalesce(sum(l.amount_minor), 0)::bigint "
                + "from ledger_entry l left join charging_session s on s.id = l.session_id "
                + "where l.country_code = current_setting('app.country', true) and l.type = 'CAPTURE' and "
                + STARTED + " >= greatest(:from, :since) and " + STARTED + " < :to group by 1", from, to, 3);
        return days.entrySet().stream().map(entry -> new DailyMoney(entry.getKey(), entry.getValue()[0],
                entry.getValue()[1], entry.getValue()[2], entry.getValue()[3])).toList();
    }

    private void collect(Map<LocalDate, long[]> days, String sql, Instant from, Instant to, int... slots) {
        jdbc.sql(sql).param("from", Timestamps.toDb(from)).param("to", Timestamps.toDb(to))
                .param("since", reporting.since()).query((rs, rowNum) -> {
                    long[] row = days.computeIfAbsent(rs.getObject(1, LocalDate.class), key -> new long[4]);
                    for (int index = 0; index < slots.length; index++) {
                        row[slots[index]] = rs.getLong(index + 2);
                    }
                    return row;
                }).list();
    }
}
