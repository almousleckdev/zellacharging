package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.FreeUsage;
import com.evcharge.customer.charging.FreeUsageRow;
import com.evcharge.customer.shared.ReportingWindow;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
class JdbcFreeUsage implements FreeUsage {

    private static final String QUERY = "select s.customer_id, c.first_name, c.last_name, count(*) as sessions, "
            + "coalesce(sum(s.energy_kwh), 0) as energy, coalesce(sum(s.cost_minor), 0)::bigint as value, "
            + "max(s.currency) as currency, max(s.currency_decimals) as decimals, max(s.created_at) as last_at "
            + "from charging_session s join customer c on c.id = s.customer_id "
            + "join customer_complimentary g on g.customer_id = s.customer_id and g.country_code = s.country_code "
            + "where s.country_code = current_setting('app.country', true) and g.category = :category "
            + "and s.state in ('COMPLETED', 'ABANDONED') and s.energy_kwh is not null "
            + "and s.created_at >= :from and s.created_at < :to "
            + "and exists (select 1 from funding_hold h where h.session_id = s.id and h.waived) "
            + "group by s.customer_id, c.first_name, c.last_name order by energy desc, s.customer_id";

    private final JdbcClient jdbc;
    private final ReportingWindow reporting;

    JdbcFreeUsage(JdbcClient jdbc, ReportingWindow reporting) {
        this.jdbc = jdbc;
        this.reporting = reporting;
    }

    @Override
    public List<FreeUsageRow> byCustomer(String category, Instant from, Instant to) {
        var floor = reporting.since();
        var start = from == null || Timestamps.toDb(from).isBefore(floor) ? floor : Timestamps.toDb(from);
        return jdbc.sql(QUERY).param("category", category).param("from", start)
                .param("to", Timestamps.toDb(to == null ? Instant.parse("9999-01-01T00:00:00Z") : to))
                .query((rs, row) -> new FreeUsageRow(rs.getObject("customer_id", UUID.class),
                        rs.getString("first_name") + " " + rs.getString("last_name"), rs.getLong("sessions"),
                        rs.getBigDecimal("energy"), rs.getLong("value"), rs.getString("currency"),
                        rs.getObject("decimals", Integer.class), Timestamps.read(rs, "last_at"))).list();
    }
}
