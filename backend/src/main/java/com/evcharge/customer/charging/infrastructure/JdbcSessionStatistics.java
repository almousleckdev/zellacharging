package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.CustomerSessions;
import com.evcharge.customer.charging.SessionFigures;
import com.evcharge.customer.charging.SessionStatistics;
import com.evcharge.customer.shared.ReportingWindow;
import org.springframework.jdbc.core.simple.JdbcClient;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
class JdbcSessionStatistics implements SessionStatistics {

    private final JdbcClient jdbc;
    private final ReportingWindow reporting;

    JdbcSessionStatistics(JdbcClient jdbc, ReportingWindow reporting) {
        this.jdbc = jdbc;
        this.reporting = reporting;
    }

    @Override
    public SessionFigures figures() {
        return jdbc.sql("select "
                        + "count(*) filter (where state in ('REQUESTED', 'START_ACCEPTED', 'ACTIVE', 'STOPPING')) "
                        + "as live, "
                        + "count(*) filter (where state = 'COMPLETED') as completed, "
                        + "count(*) filter (where started_at >= now() - interval '24 hours') as recent, "
                        + "coalesce(sum(energy_kwh) filter (where state = 'COMPLETED'), 0) as energy, "
                        + "coalesce(sum(cost_minor) filter (where state = 'COMPLETED'), 0)::bigint as revenue "
                        + "from charging_session "
                        + "where country_code = current_setting('app.country', true) and created_at >= :since")
                .param("since", reporting.since())
                .query((rs, rowNum) -> new SessionFigures(rs.getLong("live"), rs.getLong("completed"),
                        rs.getLong("recent"), rs.getBigDecimal("energy"), rs.getLong("revenue"))).single();
    }

    @Override
    public CustomerSessions ofCustomer(UUID customerId) {
        return jdbc.sql("select "
                        + "count(*) filter (where state = 'COMPLETED') as completed, "
                        + "count(*) filter (where state in ('REQUESTED', 'START_ACCEPTED', 'ACTIVE', 'STOPPING')) "
                        + "as live, "
                        + "coalesce(sum(energy_kwh) filter (where state = 'COMPLETED'), 0) as energy, "
                        + "coalesce(sum(cost_minor) filter (where state = 'COMPLETED'), 0)::bigint as spent, "
                        + "max(started_at) as last_session "
                        + "from charging_session where customer_id = :id "
                        + "and country_code = current_setting('app.country', true)")
                .param("id", customerId)
                .query((rs, rowNum) -> new CustomerSessions(rs.getLong("completed"), rs.getLong("live"),
                        rs.getBigDecimal("energy"), rs.getLong("spent"), Timestamps.read(rs, "last_session")))
                .single();
    }
}
