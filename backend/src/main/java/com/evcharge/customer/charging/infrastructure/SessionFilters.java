package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.SessionLogQuery;
import com.evcharge.customer.charging.SessionRecord;
import com.evcharge.customer.shared.LikePattern;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.OffsetDateTime;
import java.util.UUID;

final class SessionFilters {

    static final String FROM = " from charging_session s join customer c on c.id = s.customer_id "
            + "where s.country_code = current_setting('app.country', true) and s.created_at >= :since";

    static final RowMapper<SessionRecord> MAPPER = (rs, rowNum) -> new SessionRecord(
            rs.getObject("id", UUID.class), rs.getObject("customer_id", UUID.class),
            rs.getString("first_name") + " " + rs.getString("last_name"), rs.getString("station_id"),
            rs.getInt("connector_id"), rs.getString("state"), rs.getString("failure_code"),
            rs.getBigDecimal("energy_kwh"), rs.getObject("cost_minor", Long.class), rs.getString("currency"),
            rs.getObject("currency_decimals", Integer.class), rs.getBigDecimal("start_battery_percent"),
            rs.getBigDecimal("battery_percent"), Timestamps.read(rs, "started_at"),
            Timestamps.read(rs, "stopped_at"), Timestamps.read(rs, "created_at"), rs.getString("free_category"));

    private static final String TEXT_FILTER = " and (lower(s.station_id) like :pattern escape '\\' "
            + "or lower(c.first_name || ' ' || c.last_name) like :pattern escape '\\')";

    private SessionFilters() {
    }

    static String where(SessionLogQuery query) {
        return (query.state() == null ? "" : " and s.state = :state")
                + (query.station() == null ? "" : " and s.station_id = :station") + (query.text() == null ? "" : TEXT_FILTER)
                + query.filter().clause("s.created_at", "s.customer_id");
    }

    static JdbcClient.StatementSpec bind(JdbcClient.StatementSpec spec, SessionLogQuery query, OffsetDateTime since) {
        spec = query.filter().bind(spec).param("since", since);
        if (query.state() != null) {
            spec = spec.param("state", query.state());
        }
        if (query.station() != null) {
            spec = spec.param("station", query.station());
        }
        return query.text() == null ? spec : spec.param("pattern", LikePattern.contains(query.text()));
    }
}
