package com.evcharge.customer.wallet.infrastructure;

import com.evcharge.customer.shared.LikePattern;
import com.evcharge.customer.shared.Timestamps;
import com.evcharge.customer.wallet.PaymentQuery;
import com.evcharge.customer.wallet.PaymentRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.OffsetDateTime;
import java.util.UUID;

final class PaymentFilters {

    static final String COLUMNS = "p.*, c.first_name, c.last_name, w.currency, w.decimals";

    static final RowMapper<PaymentRecord> MAPPER = (rs, rowNum) -> new PaymentRecord(
            rs.getString("kind"), rs.getObject("id", UUID.class), rs.getObject("customer_id", UUID.class),
            rs.getString("first_name") + " " + rs.getString("last_name"), rs.getString("operator"),
            rs.getString("msisdn"), rs.getLong("amount_minor"), rs.getString("currency").trim(),
            rs.getInt("decimals"), rs.getString("state"), rs.getString("failure_code"), rs.getString("reference"),
            Timestamps.read(rs, "created_at"));

    private static final String SCOPE = "country_code = current_setting('app.country', true) "
            + "and created_at >= :since";
    static final String MERGED = "(select 'TOPUP' as kind, id, customer_id, operator, msisdn, amount_minor, "
            + "state, failure_code, reference, created_at from topup where " + SCOPE
            + " and (provider_ref is null or provider_ref not like 'sandbox-%')"
            + " union all select 'WITHDRAWAL', id, customer_id, operator, msisdn, amount_minor, state, "
            + "failure_code, reference, created_at from withdrawal where " + SCOPE + ") p "
            + "join customer c on c.id = p.customer_id join wallet w on w.customer_id = p.customer_id";
    private static final String TEXT_FILTER = " and (lower(p.reference) like :pattern escape '\\' "
            + "or p.msisdn like :pattern escape '\\' "
            + "or lower(c.first_name || ' ' || c.last_name) like :pattern escape '\\')";

    private PaymentFilters() {
    }

    static String where(PaymentQuery query) {
        return " where true" + (query.kind() == null ? "" : " and p.kind = :kind")
                + (query.state() == null ? "" : " and p.state = :state") + (query.text() == null ? "" : TEXT_FILTER)
                + query.filter().clause("p.created_at", "p.customer_id");
    }

    static JdbcClient.StatementSpec bind(JdbcClient.StatementSpec spec, PaymentQuery query, OffsetDateTime since) {
        spec = query.filter().bind(spec).param("since", since);
        if (query.kind() != null) {
            spec = spec.param("kind", query.kind());
        }
        if (query.state() != null) {
            spec = spec.param("state", query.state());
        }
        return query.text() == null ? spec : spec.param("pattern", LikePattern.contains(query.text()));
    }
}
