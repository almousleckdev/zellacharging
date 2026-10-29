package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.application.port.PendingEmailRepository;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcPendingEmailRepository implements PendingEmailRepository {

    private final JdbcClient jdbc;

    JdbcPendingEmailRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(UUID customerId, String email, Instant at) {
        jdbc.sql("insert into pending_email (customer_id, email, created_at) values (:id, :email, :at) "
                        + "on conflict (customer_id) do update set email = :email, created_at = :at")
                .param("id", customerId).param("email", email).param("at", Timestamps.toDb(at)).update();
    }

    @Override
    public Optional<String> find(UUID customerId) {
        return jdbc.sql("select email from pending_email where customer_id = :id").param("id", customerId)
                .query(String.class).optional();
    }

    @Override
    public void delete(UUID customerId) {
        jdbc.sql("delete from pending_email where customer_id = :id").param("id", customerId).update();
    }
}
