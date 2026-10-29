package com.evcharge.customer.wallet.infrastructure;

import com.evcharge.customer.shared.Incident;
import com.evcharge.customer.shared.IncidentFeed;
import com.evcharge.customer.shared.IncidentRowMapper;
import com.evcharge.customer.shared.LogFilter;
import com.evcharge.customer.shared.ReportingWindow;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("walletIncidentFeed")
class JdbcIncidentFeed implements IncidentFeed {

    private static final String SOURCE = "select %s as kind, %s as severity, '%s' as subject_type, p.id, "
            + "p.customer_id, c.first_name || ' ' || c.last_name as customer_name, "
            + "coalesce(p.failure_code, p.state) as code, p.amount_minor, w.currency, w.decimals, "
            + "p.created_at as at from %s p join customer c on c.id = p.customer_id "
            + "join wallet w on w.customer_id = p.customer_id where %s "
            + "and p.country_code = current_setting('app.country', true) and p.created_at >= :since";

    private static final String ALL = String.join(" union all ",
            SOURCE.formatted("'TOPUP_FAILED'", "'LOW'", "topup", "topup", "p.state = 'FAILED' "
                    + "and (p.provider_ref is null or p.provider_ref not like 'sandbox-%')"),
            SOURCE.formatted("'TOPUP_STUCK'", "'MEDIUM'", "topup", "topup", "p.state = 'PENDING' "
                    + "and p.created_at < now() - interval '30 minutes'"),
            SOURCE.formatted("'WITHDRAWAL_FAILED'", "'HIGH'", "withdrawal", "withdrawal", "p.state = 'FAILED'"),
            SOURCE.formatted("'WITHDRAWAL_REVIEW'", "'HIGH'", "withdrawal", "withdrawal", "p.state = 'REVIEW'"));

    private final JdbcClient jdbc;
    private final ReportingWindow reporting;

    JdbcIncidentFeed(JdbcClient jdbc, ReportingWindow reporting) {
        this.jdbc = jdbc;
        this.reporting = reporting;
    }

    @Override
    public List<Incident> recent(LogFilter filter, int limit) {
        return filter.bind(jdbc.sql("select * from (" + ALL + ") i where true"
                        + filter.clause("i.at", "i.customer_id") + " order by i.at desc limit :limit"))
                .param("since", reporting.since()).param("limit", limit)
                .query(IncidentRowMapper.INSTANCE).list();
    }
}
