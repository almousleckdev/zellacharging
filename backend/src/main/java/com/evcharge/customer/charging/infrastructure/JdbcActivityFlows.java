package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.ActivityFlows;
import com.evcharge.customer.charging.DailyActivity;
import com.evcharge.customer.charging.application.FeeRates;
import com.evcharge.customer.shared.Bucket;
import com.evcharge.customer.shared.LocalDays;
import com.evcharge.customer.shared.ReportingWindow;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Repository
class JdbcActivityFlows implements ActivityFlows {

    private static final String BILLED = "state in ('COMPLETED', 'ABANDONED') and cost_minor > 0 and "
            + SessionBilling.PAID;

    private final JdbcClient jdbc;
    private final FeeRates fees;
    private final ReportingWindow reporting;

    JdbcActivityFlows(JdbcClient jdbc, FeeRates fees, ReportingWindow reporting) {
        this.jdbc = jdbc;
        this.fees = fees;
        this.reporting = reporting;
    }

    @Override
    public List<DailyActivity> between(Instant from, Instant to, Bucket bucket) {
        return jdbc.sql("select " + LocalDays.bucket("created_at", bucket) + " as day, "
                        + "count(*) as sessions, count(*) filter (where state = 'COMPLETED') as completed, "
                        + "count(*) filter (where state = 'FAILED') as failed, "
                        + "coalesce(sum(energy_kwh) filter (where state = 'COMPLETED'), 0) as energy, "
                        + "coalesce(sum(cost_minor) filter (where " + BILLED + "), 0)::bigint as revenue, "
                        + "coalesce(sum(coalesce((select c2.service_fee_minor from session_charge c2 where c2.session_id = "
                        + "charging_session.id), round(cost_minor * :service / 100))) filter (where " + BILLED
                        + "), 0)::bigint as service, "
                        + "coalesce(sum(round(cost_minor * :vat / 100)) filter (where " + BILLED
                        + "), 0)::bigint as vat from charging_session "
                        + "where country_code = current_setting('app.country', true) "
                        + "and created_at >= greatest(:from, :since) and created_at < :to group by 1 order by 1")
                .param("from", Timestamps.toDb(from)).param("to", Timestamps.toDb(to))
                .param("since", reporting.since()).param("service", fees.serviceFeePercent())
                .param("vat", fees.vatPercent())
                .query((rs, rowNum) -> new DailyActivity(rs.getObject("day", LocalDate.class), rs.getLong("sessions"),
                        rs.getLong("completed"), rs.getLong("failed"), rs.getBigDecimal("energy"),
                        rs.getLong("revenue"), rs.getLong("service"), rs.getLong("vat"))).list();
    }
}
