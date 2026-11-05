package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.ChargeFeed;
import com.evcharge.customer.charging.ChargeFeedItem;
import com.evcharge.customer.charging.ChargeFeedPage;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
class JdbcChargeFeed implements ChargeFeed {

    private static final String QUERY = "select * from ("
            + "select s.id as session_id, s.transaction_id, s.customer_id, s.id_tag, s.station_id, s.connector_id, "
            + "s.state as session_state, s.started_at, s.stopped_at, s.energy_kwh, "
            + "coalesce(c.state, 'PENDING') as payment_state, c.energy_cost_minor, c.vat_minor, c.service_fee_minor, "
            + "c.total_minor, coalesce(c.currency, s.currency) as currency, "
            + "coalesce(c.decimals, s.currency_decimals) as decimals, coalesce(c.estimated, false) as estimated, "
            + "greatest(s.updated_at, coalesce(c.updated_at, s.updated_at)) as feed_at "
            + "from charging_session s left join session_charge c on c.session_id = s.id "
            + "where s.customer_id in (:ids) and s.transaction_id is not null "
            + "and s.country_code = current_setting('app.country')) f "
            + "where (f.feed_at, f.session_id) > (:at, :sid) and f.feed_at < :until order by f.feed_at, f.session_id limit :limit";

    private static final RowMapper<ChargeFeedItem> MAPPER = (rs, row) -> new ChargeFeedItem(
            rs.getObject("session_id", UUID.class), rs.getInt("transaction_id"),
            rs.getObject("customer_id", UUID.class), rs.getString("id_tag"), rs.getString("station_id"),
            rs.getInt("connector_id"), rs.getString("session_state"), Timestamps.read(rs, "started_at"),
            Timestamps.read(rs, "stopped_at"), rs.getBigDecimal("energy_kwh"), nullableLong(rs, "energy_cost_minor"),
            nullableLong(rs, "vat_minor"), nullableLong(rs, "service_fee_minor"), nullableLong(rs, "total_minor"),
            rs.getString("currency"), nullableInt(rs, "decimals"), rs.getString("payment_state"),
            rs.getBoolean("estimated"), Timestamps.read(rs, "feed_at"));

    private final JdbcClient jdbc;

    JdbcChargeFeed(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public ChargeFeedPage after(Instant updatedAt, UUID sessionId, int limit, Collection<UUID> customerIds,
                                Instant visibleBefore) {
        if (customerIds.isEmpty()) {
            return new ChargeFeedPage(List.of(), false);
        }
        List<ChargeFeedItem> rows = jdbc.sql(QUERY).param("ids", customerIds)
                .param("at", Timestamps.toDb(updatedAt)).param("sid", sessionId).param("limit", limit + 1)
                .param("until", Timestamps.toDb(visibleBefore))
                .query(MAPPER).list();
        boolean more = rows.size() > limit;
        return new ChargeFeedPage(more ? rows.subList(0, limit) : rows, more);
    }

    private static Long nullableLong(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static Integer nullableInt(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }
}
