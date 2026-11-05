package com.evcharge.customer.partner.infrastructure;

import com.evcharge.customer.partner.application.port.LinkStore;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
class JdbcLinkStore implements LinkStore {

    private final JdbcClient jdbc;

    JdbcLinkStore(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean link(String partner, UUID customerId, Instant at) {
        return jdbc.sql("insert into partner_link (partner, customer_id, linked_at) "
                        + "values (:partner, :customer, :at) on conflict do nothing")
                .param("partner", partner).param("customer", customerId).param("at", Timestamps.toDb(at))
                .update() == 1;
    }

    @Override
    public boolean unlink(String partner, UUID customerId) {
        return jdbc.sql("delete from partner_link where partner = :partner and customer_id = :customer")
                .param("partner", partner).param("customer", customerId).update() == 1;
    }

    @Override
    public List<UUID> linkedCustomerIds(String partner) {
        return jdbc.sql("select customer_id from partner_link where partner = :partner order by linked_at")
                .param("partner", partner).query(UUID.class).list();
    }
}
