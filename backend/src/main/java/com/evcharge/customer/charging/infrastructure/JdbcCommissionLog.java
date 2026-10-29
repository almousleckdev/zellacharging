package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.CommissionLog;
import com.evcharge.customer.charging.CommissionPage;
import com.evcharge.customer.charging.CommissionQuery;
import com.evcharge.customer.charging.CommissionRecord;
import com.evcharge.customer.charging.CommissionTotals;
import com.evcharge.customer.charging.application.FeeCalculator;
import com.evcharge.customer.charging.application.FeeSettings;
import com.evcharge.customer.charging.domain.FeeBreakdown;
import com.evcharge.customer.shared.ReportingWindow;
import com.evcharge.customer.shared.Timestamps;
import com.evcharge.customer.shared.LikePattern;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
class JdbcCommissionLog implements CommissionLog {

    private static final String BILLED = " from charging_session s join customer c on c.id = s.customer_id "
            + "where s.country_code = current_setting('app.country', true) and s.created_at >= :since "
            + "and s.state in ('COMPLETED', 'ABANDONED') and s.cost_minor > 0";

    private static final String TEXT_FILTER = " and (lower(s.station_id) like :pattern escape '\\' "
            + "or lower(c.first_name || ' ' || c.last_name) like :pattern escape '\\')";

    private final JdbcClient jdbc;
    private final FeeCalculator fees;
    private final FeeSettings settings;
    private final ReportingWindow reporting;

    JdbcCommissionLog(JdbcClient jdbc, FeeCalculator fees, FeeSettings settings, ReportingWindow reporting) {
        this.jdbc = jdbc;
        this.fees = fees;
        this.settings = settings;
        this.reporting = reporting;
    }

    @Override
    public CommissionPage search(CommissionQuery query) {
        String where = (query.text() == null ? "" : TEXT_FILTER)
                + query.filter().clause("coalesce(s.stopped_at, s.created_at)", "s.customer_id");
        var items = bind(jdbc.sql("select s.id, s.station_id, s.cost_minor, s.currency, s.currency_decimals, "
                + "coalesce(s.stopped_at, s.created_at) as at, c.first_name, c.last_name" + BILLED + where
                + " order by coalesce(s.stopped_at, s.created_at) desc, s.id limit :limit"), query)
                .param("limit", query.limit()).query(row()).list();
        CommissionTotals totals = totals(where, query);
        return new CommissionPage(totals, totals.sessions(), items);
    }

    private CommissionTotals totals(String where, CommissionQuery query) {
        return bind(jdbc.sql("select count(*), coalesce(sum(s.cost_minor), 0)::bigint, "
                        + "coalesce(sum(round(s.cost_minor * :service / 100)), 0)::bigint, "
                        + "coalesce(sum(round(s.cost_minor * :vat / 100)), 0)::bigint" + BILLED + where), query)
                .param("service", settings.serviceFeePercent()).param("vat", settings.vatPercent())
                .query((rs, rowNum) -> new CommissionTotals(rs.getLong(1), rs.getLong(2), rs.getLong(3),
                        rs.getLong(4), settings.serviceFeePercent(), settings.vatPercent())).single();
    }

    private JdbcClient.StatementSpec bind(JdbcClient.StatementSpec spec, CommissionQuery query) {
        spec = query.filter().bind(spec).param("since", reporting.since());
        return query.text() == null ? spec
                : spec.param("pattern", LikePattern.contains(query.text()));
    }

    private RowMapper<CommissionRecord> row() {
        return (rs, rowNum) -> {
            long electric = rs.getLong("cost_minor");
            FeeBreakdown fee = fees.of(electric);
            String currency = rs.getString("currency");
            return new CommissionRecord(rs.getObject("id", UUID.class),
                    rs.getString("first_name") + " " + rs.getString("last_name"), rs.getString("station_id"),
                    electric, fee.serviceMinor(), fee.vatMinor(), fee.totalMinor(),
                    currency == null ? null : currency.trim(), rs.getInt("currency_decimals"),
                    Timestamps.read(rs, "at"));
        };
    }
}
