package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.shared.Timestamps;
import com.evcharge.customer.charging.application.port.IdTagRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcIdTagRepository implements IdTagRepository {

    private final JdbcClient jdbc;

    JdbcIdTagRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<String> find(UUID customerId) {
        return jdbc.sql("select id_tag from customer_id_tag where customer_id = :id")
                .param("id", customerId).query(String.class).optional();
    }

    @Override
    public boolean saveIfAbsent(UUID customerId, String idTag) {
        return jdbc.sql("insert into customer_id_tag (customer_id, id_tag, created_at) "
                        + "values (:id, :tag, :now) on conflict do nothing")
                .param("id", customerId).param("tag", idTag)
                .param("now", Timestamps.toDb(Instant.now())).update() == 1;
    }
}
