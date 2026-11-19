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
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcSessionLog implements SessionLog {

    private static final Map<String, String> SORTS = Map.of("date", "s.created_at", "amount", "s.cost_minor",
            "energy", "s.energy_kwh");

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
        String where = SessionFilters.where(query);
        long total = SessionFilters.bind(jdbc.sql("select count(*)" + SessionFilters.FROM + where), query,
                reporting.since()).query(Long.class).single();
        var items = SessionFilters.bind(jdbc.sql("select s.*, c.first_name, c.last_name, " + SessionBilling.FREE_CATEGORY
                + ", " + SessionBilling.NDZELA_DRIVER + SessionFilters.FROM + where
                + query.sort().orderBy(SORTS, "s.id") + " limit :limit"), query, reporting.since())
                .param("limit", query.limit()).query(SessionFilters.MAPPER).list();
        return new SessionPage(total, items);
    }

    @Override
    public Optional<SessionDetail> find(UUID id) {
        return jdbc.sql("select s.*, c.first_name, c.last_name, " + SessionBilling.FREE_CATEGORY + ", " + SessionBilling.NDZELA_DRIVER + ", h.reserved_minor, h.captured_minor, "
                        + "h.state as hold_state from charging_session s join customer c on c.id = s.customer_id "
                        + "left join funding_hold h on h.session_id = s.id "
                        + "where s.country_code = current_setting('app.country', true) and s.id = :id")
                .param("id", id).query(this::detailOf).optional();
    }

    private SessionDetail detailOf(ResultSet rs, int rowNum) throws SQLException {
        SessionRecord record = SessionFilters.MAPPER.mapRow(rs, rowNum);
        FeeBreakdown fee = record.costMinor() == null ? null : fees.of(record.costMinor());
        return new SessionDetail(record, rs.getString("id_tag"), rs.getObject("transaction_id", Integer.class),
                rs.getString("stop_reason"), rs.getObject("price_per_kwh_minor", Long.class),
                rs.getBigDecimal("last_voltage"), rs.getBigDecimal("last_power_kw"),
                rs.getBigDecimal("last_energy_kwh"), Timestamps.read(rs, "progress_at"),
                Timestamps.read(rs, "updated_at"), rs.getObject("reserved_minor", Long.class),
                rs.getObject("captured_minor", Long.class), rs.getString("hold_state"),
                fee == null ? null : fee.serviceMinor(), fee == null ? null : fee.vatMinor());
    }
}
