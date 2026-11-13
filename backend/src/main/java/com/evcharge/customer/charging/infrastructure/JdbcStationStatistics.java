package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.StationActivity;
import com.evcharge.customer.charging.StationStatistics;
import com.evcharge.customer.shared.ReportingWindow;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
class JdbcStationStatistics implements StationStatistics {

    private final JdbcClient jdbc;
    private final ReportingWindow reporting;

    JdbcStationStatistics(JdbcClient jdbc, ReportingWindow reporting) {
        this.jdbc = jdbc;
        this.reporting = reporting;
    }

    @Override
    public Map<String, StationActivity> activity() {
        Map<String, StationActivity> byStation = new HashMap<>();
        jdbc.sql("select station_id, count(*) as sessions, "
                        + "count(*) filter (where state in ('REQUESTED', 'START_ACCEPTED', 'ACTIVE', 'STOPPING')) "
                        + "as live, coalesce(sum(energy_kwh) filter (where state = 'COMPLETED'), 0) as energy, "
                        + "coalesce(sum(cost_minor) filter (where state = 'COMPLETED' and "
                        + SessionBilling.PAID + "), 0)::bigint as revenue, "
                        + "max(created_at) as last_at from charging_session "
                        + "where country_code = current_setting('app.country', true) and created_at >= :since "
                        + "group by station_id")
                .param("since", reporting.since())
                .query((rs, rowNum) -> byStation.put(rs.getString("station_id"),
                        new StationActivity(rs.getString("station_id"), rs.getLong("sessions"), rs.getLong("live"),
                                rs.getBigDecimal("energy"), rs.getLong("revenue"), Timestamps.read(rs, "last_at"))))
                .list();
        return byStation;
    }
}
