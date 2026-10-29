package com.evcharge.customer.shared.audit;

import com.evcharge.customer.shared.ActivityEntry;
import com.evcharge.customer.shared.CustomerActivity;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
class JdbcCustomerActivity implements CustomerActivity {

    private static final String SCOPE = " from audit_record where actor_id = :customer "
            + "and country_code = current_setting('app.country', true)";

    private final JdbcClient jdbc;

    JdbcCustomerActivity(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public long count(UUID customerId) {
        return jdbc.sql("select count(*)" + SCOPE).param("customer", customerId).query(Long.class).single();
    }

    @Override
    public List<ActivityEntry> latest(UUID customerId, int limit) {
        return jdbc.sql("select action, detail, correlation_id, created_at" + SCOPE
                        + " order by created_at desc, id limit :limit")
                .param("customer", customerId).param("limit", limit)
                .query((rs, rowNum) -> new ActivityEntry(rs.getString("action"), rs.getString("detail"),
                        rs.getString("correlation_id"), Timestamps.read(rs, "created_at"))).list();
    }
}
