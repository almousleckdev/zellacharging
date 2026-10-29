package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.StationPrice;
import com.evcharge.customer.charging.StationPrices;
import com.evcharge.customer.shared.ReportingWindow;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
class JdbcStationPrices implements StationPrices {

    private final JdbcClient jdbc;
    private final ReportingWindow reporting;

    JdbcStationPrices(JdbcClient jdbc, ReportingWindow reporting) {
        this.jdbc = jdbc;
        this.reporting = reporting;
    }

    @Override
    public List<StationPrice> applied() {
        return jdbc.sql("select station_id, price_per_kwh_minor, count(*) as sessions, max(created_at) as last_used "
                        + "from charging_session where price_per_kwh_minor is not null "
                        + "and country_code = current_setting('app.country', true) and created_at >= :since "
                        + "group by station_id, price_per_kwh_minor order by station_id, last_used desc")
                .param("since", reporting.since())
                .query((rs, rowNum) -> new StationPrice(rs.getString("station_id"),
                        rs.getLong("price_per_kwh_minor"), rs.getLong("sessions"),
                        Timestamps.read(rs, "last_used"))).list();
    }
}
