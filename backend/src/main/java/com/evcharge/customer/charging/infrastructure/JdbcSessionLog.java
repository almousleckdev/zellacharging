package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.SessionLog;
import com.evcharge.customer.charging.SessionLogQuery;
import com.evcharge.customer.charging.SessionPage;
import com.evcharge.customer.charging.SessionRecord;
import com.evcharge.customer.shared.ReportingWindow;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
class JdbcSessionLog implements SessionLog {

    private static final String FROM = " from charging_session s join customer c on c.id = s.customer_id "
            + "where s.country_code = current_setting('app.country', true) and s.created_at >= :since";
    private static final String TEXT_FILTER = " and (lower(s.station_id) like :pattern escape '\\' "
            + "or lower(c.first_name || ' ' || c.last_name) like :pattern escape '\\')";

    private static final RowMapper<SessionRecord> MAPPER = (rs, rowNum) -> new SessionRecord(
            rs.getObject("id", UUID.class), rs.getObject("customer_id", UUID.class),
            rs.getString("first_name") + " " + rs.getString("last_name"), rs.getString("station_id"),
            rs.getInt("connector_id"), rs.getString("state"), rs.getString("failure_code"),
            rs.getBigDecimal("energy_kwh"), rs.getObject("cost_minor", Long.class), rs.getString("currency"),
            rs.getObject("currency_decimals", Integer.class), rs.getBigDecimal("start_battery_percent"),
            rs.getBigDecimal("battery_percent"), Timestamps.read(rs, "started_at"),
            Timestamps.read(rs, "stopped_at"), Timestamps.read(rs, "created_at"));

    private final JdbcClient jdbc;
    private final ReportingWindow reporting;

    JdbcSessionLog(JdbcClient jdbc, ReportingWindow reporting) {
        this.jdbc = jdbc;
        this.reporting = reporting;
    }

    @Override
    public SessionPage search(SessionLogQuery query) {
        String where = (query.state() == null ? "" : " and s.state = :state")
                + (query.text() == null ? "" : TEXT_FILTER);
        long total = bind(jdbc.sql("select count(*)" + FROM + where), query).param("since", reporting.since())
                .query(Long.class).single();
        var items = bind(jdbc.sql("select s.*, c.first_name, c.last_name" + FROM + where
                + " order by s.created_at desc, s.id limit :limit"), query)
                .param("limit", query.limit()).param("since", reporting.since()).query(MAPPER).list();
        return new SessionPage(total, items);
    }

    private static JdbcClient.StatementSpec bind(JdbcClient.StatementSpec spec, SessionLogQuery query) {
        if (query.state() != null) {
            spec = spec.param("state", query.state());
        }
        if (query.text() != null) {
            spec = spec.param("pattern", "%" + escape(query.text().toLowerCase()) + "%");
        }
        return spec;
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
