package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.UsageFigures;
import com.evcharge.customer.charging.UsageSummary;
import com.evcharge.customer.charging.UsageTotals;
import com.evcharge.customer.shared.ReportingWindow;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

@Repository
class JdbcUsageFigures implements UsageFigures {

    private static final String ZONE = "(select time_zone from country where code = current_setting('app.country', true))";
    private static final String ENERGY = "coalesce(x.energy_kwh, x.last_energy_kwh, 0)";
    private static final String BILLED = "x.state in ('COMPLETED', 'ABANDONED') and x.cost_minor > 0 and not x.free";
    private static final String[] PERIODS = {"today", "yesterday", "week", "month"};
    private static final String[] CONDITIONS = {"x.d = b.t", "x.d = b.t - 1", "x.d >= b.w", "x.d >= b.m"};

    private final JdbcClient jdbc;
    private final ReportingWindow reporting;

    JdbcUsageFigures(JdbcClient jdbc, ReportingWindow reporting) {
        this.jdbc = jdbc;
        this.reporting = reporting;
    }

    @Override
    public UsageSummary summary(String station, UUID customerId) {
        var spec = jdbc.sql(sql(station != null, customerId != null)).param("since", reporting.since());
        if (station != null) {
            spec = spec.param("station", station);
        }
        if (customerId != null) {
            spec = spec.param("customer", customerId);
        }
        return spec.query(JdbcUsageFigures::read).single();
    }

    private static String sql(boolean byStation, boolean byCustomer) {
        StringBuilder columns = new StringBuilder("b.t");
        for (int index = 0; index < PERIODS.length; index++) {
            String when = " filter (where " + CONDITIONS[index] + ")";
            columns.append(", count(x.id)").append(when).append(" as ").append(PERIODS[index]).append("_n")
                    .append(", coalesce(sum(").append(ENERGY).append(")").append(when).append(", 0) as ")
                    .append(PERIODS[index]).append("_kwh")
                    .append(", coalesce(sum(x.cost_minor) filter (where ").append(CONDITIONS[index]).append(" and ")
                    .append(BILLED).append("), 0)::bigint as ").append(PERIODS[index]).append("_money")
                    .append(", coalesce(sum(").append(ENERGY).append(") filter (where ").append(CONDITIONS[index])
                    .append(" and x.free_category = 'NDZELA_STAFF'), 0) as ").append(PERIODS[index]).append("_staff");
        }
        return "with local as (select (now() at time zone " + ZONE + ")::date as t), b as (select t, "
                + "t - (extract(isodow from t)::int - 1) as w, date_trunc('month', t)::date as m from local) "
                + "select " + columns + " from b left join (select s.*, "
                + "(s.created_at at time zone " + ZONE + ")::date as d, exists (select 1 from funding_hold h "
                + "where h.session_id = s.id and h.waived) as free, " + SessionBilling.FREE_CATEGORY
                + " from charging_session s where s.country_code = current_setting('app.country', true) "
                + "and s.created_at >= :since and s.created_at >= now() - interval '45 days'"
                + (byStation ? " and s.station_id = :station" : "") + (byCustomer ? " and s.customer_id = :customer" : "")
                + ") x on true group by b.t, b.w, b.m";
    }

    private static UsageSummary read(ResultSet rs, int rowNum) throws SQLException {
        return new UsageSummary(rs.getObject("t", LocalDate.class), totals(rs, "today"), totals(rs, "yesterday"),
                totals(rs, "week"), totals(rs, "month"));
    }

    private static UsageTotals totals(ResultSet rs, String period) throws SQLException {
        return new UsageTotals(rs.getLong(period + "_n"), rs.getBigDecimal(period + "_kwh"),
                rs.getLong(period + "_money"), rs.getBigDecimal(period + "_staff"));
    }
}
