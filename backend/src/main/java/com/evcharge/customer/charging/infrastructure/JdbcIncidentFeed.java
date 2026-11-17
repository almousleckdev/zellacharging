package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.shared.Incident;
import com.evcharge.customer.shared.IncidentFeed;
import com.evcharge.customer.shared.IncidentRowMapper;
import com.evcharge.customer.shared.LogFilter;
import com.evcharge.customer.shared.ReportingWindow;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("chargingIncidentFeed")
class JdbcIncidentFeed implements IncidentFeed {

    private static final String SESSIONS = "select case when s.state = 'FAILED' then 'SESSION_FAILED' "
            + "else 'SESSION_ABANDONED' end as kind, case when s.state = 'FAILED' then 'MEDIUM' when s.failure_code = 'CHARGER_OFFLINE' then 'HIGH' else 'LOW' end "
            + "as severity, 'session' as subject_type, s.id as subject_id, s.customer_id, "
            + "c.first_name || ' ' || c.last_name as customer_name, coalesce(s.failure_code, s.stop_reason) as code, "
            + "s.cost_minor as amount, s.currency, s.currency_decimals as decimals, "
            + "coalesce(s.stopped_at, s.created_at) as at from charging_session s "
            + "join customer c on c.id = s.customer_id where s.state in ('FAILED', 'ABANDONED') "
            + "and s.country_code = current_setting('app.country', true) and s.created_at >= :since";
    private static final String HOLDS = "select 'HOLD_REVIEW', 'HIGH', 'session', h.session_id, h.customer_id, "
            + "c.first_name || ' ' || c.last_name, 'HOLD_REVIEW', h.reserved_minor, w.currency, w.decimals, "
            + "h.created_at from funding_hold h join customer c on c.id = h.customer_id "
            + "join wallet w on w.customer_id = h.customer_id where h.state = 'REVIEW' "
            + "and h.country_code = current_setting('app.country', true) and h.created_at >= :since";

    private final JdbcClient jdbc;
    private final ReportingWindow reporting;

    JdbcIncidentFeed(JdbcClient jdbc, ReportingWindow reporting) {
        this.jdbc = jdbc;
        this.reporting = reporting;
    }

    @Override
    public List<Incident> recent(LogFilter filter, int limit) {
        return filter.bind(jdbc.sql("select * from (" + SESSIONS + " union all " + HOLDS + ") i where true"
                        + filter.clause("i.at", "i.customer_id") + " order by i.at desc limit :limit"))
                .param("since", reporting.since()).param("limit", limit)
                .query(IncidentRowMapper.INSTANCE).list();
    }
}
