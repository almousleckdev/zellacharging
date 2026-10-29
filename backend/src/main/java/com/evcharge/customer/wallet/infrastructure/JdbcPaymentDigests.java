package com.evcharge.customer.wallet.infrastructure;

import com.evcharge.customer.shared.ReportingWindow;
import com.evcharge.customer.wallet.PaymentDigest;
import com.evcharge.customer.wallet.PaymentDigestPage;
import com.evcharge.customer.wallet.PaymentDigests;
import com.evcharge.customer.wallet.PaymentQuery;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
class JdbcPaymentDigests implements PaymentDigests {

    private static final Map<String, String> SORTS = Map.of("last", "a.last_at", "count", "a.n", "spent", "a.topups");

    private final JdbcClient jdbc;
    private final ReportingWindow reporting;

    JdbcPaymentDigests(JdbcClient jdbc, ReportingWindow reporting) {
        this.jdbc = jdbc;
        this.reporting = reporting;
    }

    @Override
    public PaymentDigestPage search(PaymentQuery query) {
        String where = PaymentFilters.where(query);
        String scoped = "select " + PaymentFilters.COLUMNS + " from " + PaymentFilters.MERGED + where;
        long total = PaymentFilters.bind(jdbc.sql("select count(distinct p.customer_id) from "
                + PaymentFilters.MERGED + where), query, reporting.since()).query(Long.class).single();
        var items = PaymentFilters.bind(jdbc.sql("with scoped as (" + scoped + "), agg as ("
                + "select customer_id, count(*) as n, "
                + "coalesce(sum(amount_minor) filter (where kind = 'TOPUP' and state = 'SUCCEEDED'), 0)::bigint "
                + "as topups, coalesce(sum(amount_minor) filter (where kind = 'WITHDRAWAL' and state = 'PAID'), 0)"
                + "::bigint as withdrawn, max(created_at) as last_at from scoped group by customer_id), latest as ("
                + "select distinct on (customer_id) * from scoped order by customer_id, created_at desc, id) "
                + "select a.n, a.topups, a.withdrawn, l.* from agg a join latest l on l.customer_id = a.customer_id"
                + query.sort().orderBy(SORTS, "a.customer_id") + " limit :limit"), query, reporting.since())
                .param("limit", query.limit())
                .query((rs, rowNum) -> {
                    var last = PaymentFilters.MAPPER.mapRow(rs, rowNum);
                    return new PaymentDigest(last.customerName(), rs.getLong("n"), rs.getLong("topups"),
                            rs.getLong("withdrawn"), last);
                }).list();
        return new PaymentDigestPage(total, items);
    }
}
