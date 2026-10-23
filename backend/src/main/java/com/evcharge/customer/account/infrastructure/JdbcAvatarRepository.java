package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.application.port.AvatarRepository;
import com.evcharge.customer.account.domain.Avatar;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcAvatarRepository implements AvatarRepository {

    private final JdbcClient jdbc;

    JdbcAvatarRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(UUID customerId, Avatar avatar, Instant at) {
        jdbc.sql("""
                insert into customer_avatar (customer_id, content_type, data, updated_at)
                values (:id, :type, :data, :at)
                on conflict (customer_id) do update
                    set content_type = excluded.content_type, data = excluded.data, updated_at = excluded.updated_at
                """)
                .param("id", customerId).param("type", avatar.contentType()).param("data", avatar.data())
                .param("at", Timestamps.toDb(at)).update();
    }

    @Override
    public Optional<Avatar> find(UUID customerId) {
        return jdbc.sql("select content_type, data from customer_avatar where customer_id = :id")
                .param("id", customerId)
                .query((rs, rowNum) -> new Avatar(rs.getString("content_type"), rs.getBytes("data"))).optional();
    }

    @Override
    public void delete(UUID customerId) {
        jdbc.sql("delete from customer_avatar where customer_id = :id").param("id", customerId).update();
    }
}
