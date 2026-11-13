package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.application.port.PendingPhoneRepository;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcPendingPhoneRepository implements PendingPhoneRepository {

    private final JdbcClient jdbc;

    JdbcPendingPhoneRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(UUID customerId, String phoneE164, Instant at) {
        jdbc.sql("insert into pending_phone (customer_id, phone_e164, created_at) values (:id, :phone, :at) "
                        + "on conflict (customer_id) do update set phone_e164 = :phone, created_at = :at")
                .param("id", customerId).param("phone", phoneE164).param("at", Timestamps.toDb(at)).update();
    }

    @Override
    public Optional<String> find(UUID customerId) {
        return jdbc.sql("select phone_e164 from pending_phone where customer_id = :id").param("id", customerId)
                .query(String.class).optional();
    }

    @Override
    public void delete(UUID customerId) {
        jdbc.sql("delete from pending_phone where customer_id = :id").param("id", customerId).update();
    }
}
