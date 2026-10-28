package com.evcharge.customer.wallet.infrastructure;

import com.evcharge.customer.shared.ReportingWindow;
import com.evcharge.customer.shared.Timestamps;
import com.evcharge.customer.wallet.PaymentLog;
import com.evcharge.customer.wallet.PaymentPage;
import com.evcharge.customer.wallet.PaymentQuery;
import com.evcharge.customer.wallet.PaymentRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
class JdbcPaymentLog implements PaymentLog {

    private static final String SCOPE = "country_code = current_setting('app.country', true) "
            + "and created_at >= :since";
    private static final String MERGED = "(select 'TOPUP' as kind, id, customer_id, operator, msisdn, amount_minor, "
            + "state, failure_code, reference, created_at from topup where " + SCOPE
            + " and (provider_ref is null or provider_ref not like 'sandbox-%')"
            + " union all select 'WITHDRAWAL', id, customer_id, operator, msisdn, amount_minor, state, "
            + "failure_code, reference, created_at from withdrawal where " + SCOPE + ") p "
            + "join customer c on c.id = p.customer_id join wallet w on w.customer_id = p.customer_id";
    private static final String TEXT_FILTER = " and (lower(p.reference) like :pattern escape '\\' "
            + "or p.msisdn like :pattern escape '\\' "
            + "or lower(c.first_name || ' ' || c.last_name) like :pattern escape '\\')";

    private static final RowMapper<PaymentRecord> MAPPER = (rs, rowNum) -> new PaymentRecord(
            rs.getString("kind"), rs.getObject("id", UUID.class), rs.getObject("customer_id", UUID.class),
            rs.getString("first_name") + " " + rs.getString("last_name"), rs.getString("operator"),
            rs.getString("msisdn"), rs.getLong("amount_minor"), rs.getString("currency").trim(),
            rs.getInt("decimals"), rs.getString("state"), rs.getString("failure_code"), rs.getString("reference"),
            Timestamps.read(rs, "created_at"));

    private final JdbcClient jdbc;
    private final ReportingWindow reporting;

    JdbcPaymentLog(JdbcClient jdbc, ReportingWindow reporting) {
        this.jdbc = jdbc;
        this.reporting = reporting;
    }

    @Override
    public PaymentPage search(PaymentQuery query) {
        String where = " where true" + (query.kind() == null ? "" : " and p.kind = :kind")
                + (query.state() == null ? "" : " and p.state = :state")
                + (query.text() == null ? "" : TEXT_FILTER);
        long total = bind(jdbc.sql("select count(*) from " + MERGED + where), query)
                .param("since", reporting.since()).query(Long.class).single();
        var items = bind(jdbc.sql("select p.*, c.first_name, c.last_name, w.currency, w.decimals from " + MERGED
                + where + " order by p.created_at desc, p.id limit :limit"), query)
                .param("limit", query.limit()).param("since", reporting.since()).query(MAPPER).list();
        return new PaymentPage(total, items);
    }

    private static JdbcClient.StatementSpec bind(JdbcClient.StatementSpec spec, PaymentQuery query) {
        if (query.kind() != null) {
            spec = spec.param("kind", query.kind());
        }
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
