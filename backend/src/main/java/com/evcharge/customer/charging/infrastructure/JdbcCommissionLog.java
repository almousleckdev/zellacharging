package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.CommissionDigest;
import com.evcharge.customer.charging.CommissionLog;
import com.evcharge.customer.charging.CommissionPage;
import com.evcharge.customer.charging.CommissionQuery;
import com.evcharge.customer.charging.CommissionTotals;
import com.evcharge.customer.charging.application.FeeRates;
import com.evcharge.customer.shared.LikePattern;
import com.evcharge.customer.shared.ReportingWindow;
import com.evcharge.customer.shared.Timestamps;
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
    private static final String SERVICE = "coalesce(sum(coalesce((select c2.service_fee_minor from session_charge c2 "
            + "where c2.session_id = s.id), round(s.cost_minor * :service / 100))), 0)::bigint";
    private static final String VAT = "coalesce(sum(round(s.cost_minor * :vat / 100)), 0)::bigint";

    private final JdbcClient jdbc;
    private final FeeRates settings;
    private final ReportingWindow reporting;

    JdbcCommissionLog(JdbcClient jdbc, FeeRates settings, ReportingWindow reporting) {
        this.jdbc = jdbc;
        this.settings = settings;
        this.reporting = reporting;
    }

    @Override
    public CommissionPage search(CommissionQuery query) {
        String where = (query.text() == null ? "" : TEXT_FILTER)
                + query.filter().clause("coalesce(s.stopped_at, s.created_at)", "s.customer_id");
        var items = bind(jdbc.sql("select s.customer_id, c.first_name, c.last_name, count(*) as n, "
                + "coalesce(sum(s.cost_minor), 0)::bigint as electric, " + SERVICE + " as service, " + VAT
                + " as vat, max(s.currency) as currency, max(s.currency_decimals) as decimals, "
                + "max(coalesce(s.stopped_at, s.created_at)) as last_at" + BILLED + where
                + " group by s.customer_id, c.first_name, c.last_name order by last_at desc, s.customer_id "
                + "limit :limit"), query).param("limit", query.limit())
                .query((rs, rowNum) -> new CommissionDigest(rs.getObject("customer_id", UUID.class),
                        rs.getString("first_name") + " " + rs.getString("last_name"), rs.getLong("n"),
                        rs.getLong("electric"), rs.getLong("service"), rs.getLong("vat"),
                        rs.getString("currency") == null ? null : rs.getString("currency").trim(),
                        rs.getInt("decimals"), Timestamps.read(rs, "last_at"))).list();
        long customers = bind(jdbc.sql("select count(distinct s.customer_id)" + BILLED + where), query)
                .query(Long.class).single();
        return new CommissionPage(totals(where, query), customers, items);
    }

    private CommissionTotals totals(String where, CommissionQuery query) {
        return bind(jdbc.sql("select count(*), coalesce(sum(s.cost_minor), 0)::bigint, " + SERVICE + ", " + VAT
                + BILLED + where), query)
                .query((rs, rowNum) -> new CommissionTotals(rs.getLong(1), rs.getLong(2), rs.getLong(3),
                        rs.getLong(4), settings.serviceFeePercent(), settings.vatPercent())).single();
    }

    private JdbcClient.StatementSpec bind(JdbcClient.StatementSpec spec, CommissionQuery query) {
        spec = query.filter().bind(spec).param("since", reporting.since()).param("service", settings.serviceFeePercent())
                .param("vat", settings.vatPercent());
        return query.text() == null ? spec : spec.param("pattern", LikePattern.contains(query.text()));
    }
}
