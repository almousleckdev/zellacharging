package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.SessionDetail;
import com.evcharge.customer.charging.SessionLog;
import com.evcharge.customer.charging.SessionLogQuery;
import com.evcharge.customer.charging.SessionPage;
import com.evcharge.customer.charging.SessionRecord;
import com.evcharge.customer.charging.application.FeeCalculator;
import com.evcharge.customer.charging.domain.FeeBreakdown;
import com.evcharge.customer.shared.ReportingWindow;
import com.evcharge.customer.shared.Timestamps;
import com.evcharge.customer.shared.LikePattern;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcSessionLog implements SessionLog {

    private static final String FROM = " from charging_session s join customer c on c.id = s.customer_id "
            + "where s.country_code = current_setting('app.country', true) and s.created_at >= :since";
    private static final Map<String, String> SORTS = Map.of("date", "s.created_at", "amount", "s.cost_minor",
            "energy", "s.energy_kwh");
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
    private final FeeCalculator fees;
    private final ReportingWindow reporting;

    JdbcSessionLog(JdbcClient jdbc, FeeCalculator fees, ReportingWindow reporting) {
        this.jdbc = jdbc;
        this.fees = fees;
        this.reporting = reporting;
    }

    @Override
    public SessionPage search(SessionLogQuery query) {
        String where = (query.state() == null ? "" : " and s.state = :state")
                + (query.text() == null ? "" : TEXT_FILTER) + query.filter().clause("s.created_at", "s.customer_id");
        long total = bind(jdbc.sql("select count(*)" + FROM + where), query).param("since", reporting.since())
                .query(Long.class).single();
        var items = bind(jdbc.sql("select s.*, c.first_name, c.last_name" + FROM + where
                + query.sort().orderBy(SORTS, "s.id") + " limit :limit"), query)
                .param("limit", query.limit()).param("since", reporting.since()).query(MAPPER).list();
        return new SessionPage(total, items);
    }

    @Override
    public Optional<SessionDetail> find(UUID id) {
        return jdbc.sql("select s.*, c.first_name, c.last_name, h.reserved_minor, h.captured_minor, "
                        + "h.state as hold_state from charging_session s join customer c on c.id = s.customer_id "
                        + "left join funding_hold h on h.session_id = s.id "
                        + "where s.country_code = current_setting('app.country', true) and s.id = :id")
                .param("id", id).query(this::detailOf).optional();
    }

    private SessionDetail detailOf(ResultSet rs, int rowNum) throws SQLException {
        SessionRecord record = MAPPER.mapRow(rs, rowNum);
        FeeBreakdown fee = record.costMinor() == null ? null : fees.of(record.costMinor());
        return new SessionDetail(record, rs.getString("id_tag"), rs.getObject("transaction_id", Integer.class),
                rs.getString("stop_reason"), rs.getObject("price_per_kwh_minor", Long.class),
                rs.getBigDecimal("last_voltage"), rs.getBigDecimal("last_power_kw"),
                rs.getBigDecimal("last_energy_kwh"), Timestamps.read(rs, "progress_at"),
                Timestamps.read(rs, "updated_at"), rs.getObject("reserved_minor", Long.class),
                rs.getObject("captured_minor", Long.class), rs.getString("hold_state"),
                fee == null ? null : fee.serviceMinor(), fee == null ? null : fee.vatMinor());
    }

    private static JdbcClient.StatementSpec bind(JdbcClient.StatementSpec spec, SessionLogQuery query) {
        spec = query.filter().bind(spec);
        if (query.state() != null) {
            spec = spec.param("state", query.state());
        }
        if (query.text() != null) {
            spec = spec.param("pattern", LikePattern.contains(query.text()));
        }
        return spec;
    }
}
