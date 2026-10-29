package com.evcharge.customer.wallet.infrastructure;

import com.evcharge.customer.shared.ReportingWindow;
import com.evcharge.customer.shared.Timestamps;
import com.evcharge.customer.wallet.PaymentDetail;
import com.evcharge.customer.wallet.PaymentLog;
import com.evcharge.customer.wallet.PaymentPage;
import com.evcharge.customer.wallet.PaymentQuery;
import com.evcharge.customer.wallet.PaymentRecord;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcPaymentLog implements PaymentLog {

    private static final Map<String, String> SORTS = Map.of("date", "p.created_at", "amount", "p.amount_minor");

    private final JdbcClient jdbc;
    private final ReportingWindow reporting;

    JdbcPaymentLog(JdbcClient jdbc, ReportingWindow reporting) {
        this.jdbc = jdbc;
        this.reporting = reporting;
    }

    @Override
    public PaymentPage search(PaymentQuery query) {
        String where = PaymentFilters.where(query);
        long total = PaymentFilters.bind(jdbc.sql("select count(*) from " + PaymentFilters.MERGED + where), query,
                reporting.since()).query(Long.class).single();
        var items = PaymentFilters.bind(jdbc.sql("select " + PaymentFilters.COLUMNS + " from " + PaymentFilters.MERGED
                + where + query.sort().orderBy(SORTS, "p.id") + " limit :limit"), query, reporting.since())
                .param("limit", query.limit()).query(PaymentFilters.MAPPER).list();
        return new PaymentPage(total, items);
    }

    @Override
    public Optional<PaymentDetail> find(String kind, UUID id) {
        String table = "WITHDRAWAL".equals(kind) ? "withdrawal" : "topup";
        return jdbc.sql("select :kind as kind, p.id, p.customer_id, p.operator, p.msisdn, p.amount_minor, p.state, "
                        + "p.failure_code, p.reference, p.created_at, p.provider_ref, p.updated_at, c.first_name, "
                        + "c.last_name, w.currency, w.decimals from " + table + " p "
                        + "join customer c on c.id = p.customer_id join wallet w on w.customer_id = p.customer_id "
                        + "where p.country_code = current_setting('app.country', true) and p.id = :id")
                .param("kind", table.toUpperCase()).param("id", id)
                .query((rs, rowNum) -> new PaymentDetail(PaymentFilters.MAPPER.mapRow(rs, rowNum), rs.getString("provider_ref"),
                        Timestamps.read(rs, "updated_at"))).optional();
    }
}
