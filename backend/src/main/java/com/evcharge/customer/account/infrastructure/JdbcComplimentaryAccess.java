package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.Complimentary;
import com.evcharge.customer.account.ComplimentaryAccess;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.FreeCharging;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcComplimentaryAccess implements ComplimentaryAccess, FreeCharging {

    private static final String SELECT = "select g.customer_id, c.first_name, c.last_name, g.reason, g.granted_by, "
            + "g.granted_at, g.expires_at from customer_complimentary g join customer c on c.id = g.customer_id "
            + "and c.country_code = g.country_code where g.country_code = current_setting('app.country', true)";

    private static final RowMapper<Complimentary> MAPPER = (rs, row) -> new Complimentary(
            rs.getObject("customer_id", UUID.class), rs.getString("first_name"), rs.getString("last_name"),
            rs.getString("reason"), rs.getObject("granted_by", UUID.class), Timestamps.read(rs, "granted_at"),
            Timestamps.read(rs, "expires_at"));

    private final JdbcClient jdbc;
    private final Clock clock;

    JdbcComplimentaryAccess(JdbcClient jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Override
    public void grant(UUID customerId, UUID grantedBy, String reason, Instant expiresAt) {
        int written = jdbc.sql("insert into customer_complimentary (customer_id, granted_by, reason, granted_at, "
                        + "expires_at) select id, :by, :reason, :now, :expires from customer where id = :id "
                        + "and country_code = current_setting('app.country', true) and status <> 'DELETED' "
                        + "on conflict (customer_id) do update set granted_by = excluded.granted_by, "
                        + "reason = excluded.reason, granted_at = excluded.granted_at, expires_at = excluded.expires_at")
                .param("id", customerId).param("by", grantedBy).param("reason", reason)
                .param("now", Timestamps.toDb(clock.instant())).param("expires", Timestamps.toDb(expiresAt)).update();
        if (written == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND", "Customer not found");
        }
    }

    @Override
    public boolean revoke(UUID customerId) {
        return jdbc.sql("delete from customer_complimentary where customer_id = :id "
                + "and country_code = current_setting('app.country', true)").param("id", customerId).update() > 0;
    }

    @Override
    public Optional<Complimentary> find(UUID customerId) {
        return jdbc.sql(SELECT + " and g.customer_id = :id").param("id", customerId).query(MAPPER).optional();
    }

    @Override
    public List<Complimentary> all() {
        return jdbc.sql(SELECT + " order by g.granted_at desc, g.customer_id").query(MAPPER).list();
    }

    @Override
    public boolean allowed(UUID customerId) {
        return jdbc.sql("select count(*) from customer_complimentary where customer_id = :id "
                        + "and country_code = current_setting('app.country', true) "
                        + "and (expires_at is null or expires_at > :now)")
                .param("id", customerId).param("now", Timestamps.toDb(clock.instant())).query(Integer.class)
                .single() > 0;
    }
}
