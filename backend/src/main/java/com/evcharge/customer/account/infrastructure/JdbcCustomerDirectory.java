package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.CustomerDirectory;
import com.evcharge.customer.account.CustomerPage;
import com.evcharge.customer.account.CustomerQuery;
import com.evcharge.customer.account.CustomerRecord;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcCustomerDirectory implements CustomerDirectory {

    private static final String SCOPE = "country_code = current_setting('app.country', true) and status <> 'DELETED'";
    private static final String TEXT_FILTER = " and (lower(first_name || ' ' || last_name) like :pattern escape '\\' "
            + "or lower(coalesce(email, '')) like :pattern escape '\\' "
            + "or coalesce(phone_e164, '') like :pattern escape '\\')";
    private static final String COLUMNS = "id, first_name, last_name, email, phone_e164, status, "
            + "email_verified_at, phone_verified_at, created_at";

    private static final RowMapper<CustomerRecord> MAPPER = (rs, rowNum) -> new CustomerRecord(
            rs.getObject("id", UUID.class), rs.getString("first_name"), rs.getString("last_name"),
            rs.getString("email"), rs.getString("phone_e164"), rs.getString("status"),
            rs.getObject("email_verified_at") != null, rs.getObject("phone_verified_at") != null,
            Timestamps.read(rs, "created_at"));

    private final JdbcClient jdbc;

    JdbcCustomerDirectory(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public CustomerPage search(CustomerQuery query) {
        String where = SCOPE + statusFilter(query) + textFilter(query);
        long total = bind(jdbc.sql("select count(*) from customer where " + where), query).query(Long.class).single();
        var items = bind(jdbc.sql("select " + COLUMNS + " from customer where " + where
                + " order by created_at desc, id limit :limit offset :offset"), query)
                .param("limit", query.limit()).param("offset", query.offset()).query(MAPPER).list();
        return new CustomerPage(total, items);
    }

    @Override
    public Optional<CustomerRecord> find(UUID id) {
        return jdbc.sql("select " + COLUMNS + " from customer where " + SCOPE + " and id = :id")
                .param("id", id).query(MAPPER).optional();
    }

    private static String statusFilter(CustomerQuery query) {
        return query.status() == null ? "" : " and status = :status";
    }

    private static String textFilter(CustomerQuery query) {
        return query.text() == null ? "" : TEXT_FILTER;
    }

    private static JdbcClient.StatementSpec bind(JdbcClient.StatementSpec spec, CustomerQuery query) {
        if (query.status() != null) {
            spec = spec.param("status", query.status());
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
