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

    private static final String QUERY = """
            select g.customer_id, c.first_name, c.last_name, count(s.id) as sessions,
                   coalesce(sum(s.energy_kwh), 0) as energy, coalesce(sum(s.cost_minor), 0)::bigint as value,
                   max(s.currency) as currency, max(s.currency_decimals) as decimals, max(s.created_at) as last_at
            from customer_complimentary g
            join customer c on c.id = g.customer_id and c.country_code = g.country_code and c.status <> 'DELETED'
            left join charging_session s on s.customer_id = g.customer_id and s.country_code = g.country_code
                and s.state in ('COMPLETED', 'ABANDONED') and s.energy_kwh is not null
                and s.created_at >= :from and s.created_at < :to
                and exists (select 1 from funding_hold h where h.session_id = s.id and h.waived)
            where g.country_code = current_setting('app.country', true) and g.category = :category
            group by g.customer_id, g.expires_at, c.first_name, c.last_name
            having count(s.id) > 0 or g.expires_at is null or g.expires_at > now()
            order by energy desc, g.customer_id
            """;

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
