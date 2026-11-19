package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.SessionDigest;
import com.evcharge.customer.charging.SessionDigestPage;
import com.evcharge.customer.charging.SessionDigests;
import com.evcharge.customer.charging.SessionLogQuery;
import com.evcharge.customer.shared.ReportingWindow;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
class JdbcSessionDigests implements SessionDigests {

    private static final Map<String, String> SORTS = Map.of("last", "a.last_at", "count", "a.n", "spent", "a.spent");

    private final JdbcClient jdbc;
    private final ReportingWindow reporting;

    JdbcSessionDigests(JdbcClient jdbc, ReportingWindow reporting) {
        this.jdbc = jdbc;
        this.reporting = reporting;
    }

    @Override
    public SessionDigestPage search(SessionLogQuery query) {
        String scoped = "select s.*, c.first_name, c.last_name, exists (select 1 from funding_hold h "
                + "where h.session_id = s.id and h.waived) as free, " + SessionBilling.FREE_CATEGORY + ", "
                + SessionBilling.NDZELA_DRIVER + SessionFilters.FROM + SessionFilters.where(query);
        long total = SessionFilters.bind(jdbc.sql("select count(distinct s.customer_id)" + SessionFilters.FROM
                + SessionFilters.where(query)), query, reporting.since()).query(Long.class).single();
        var items = SessionFilters.bind(jdbc.sql("with scoped as (" + scoped + "), agg as ("
                + "select customer_id, count(*) as n, coalesce(sum(energy_kwh) filter (where state = 'COMPLETED'), 0) "
                + "as energy, coalesce(sum(cost_minor) filter (where state = 'COMPLETED' and not free), 0)::bigint "
                + "as spent, "
                + "max(created_at) as last_at from scoped group by customer_id), latest as ("
                + "select distinct on (customer_id) * from scoped order by customer_id, created_at desc, id) "
                + "select a.n, a.energy, a.spent, l.* from agg a join latest l on l.customer_id = a.customer_id"
                + query.sort().orderBy(SORTS, "a.customer_id") + " limit :limit"), query, reporting.since())
                .param("limit", query.limit())
                .query((rs, rowNum) -> {
                    var last = SessionFilters.MAPPER.mapRow(rs, rowNum);
                    return new SessionDigest(last.customerName(), rs.getLong("n"), rs.getBigDecimal("energy"),
                            rs.getLong("spent"), last);
                }).list();
        return new SessionDigestPage(total, items);
    }
}
