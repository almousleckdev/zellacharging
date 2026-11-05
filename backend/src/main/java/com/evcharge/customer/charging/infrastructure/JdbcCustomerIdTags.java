package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.CustomerIdTags;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
class JdbcCustomerIdTags implements CustomerIdTags {

    private final JdbcClient jdbc;

    JdbcCustomerIdTags(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Map<UUID, String> existing(Collection<UUID> customerIds) {
        Map<UUID, String> tags = new HashMap<>();
        if (customerIds.isEmpty()) {
            return tags;
        }
        jdbc.sql("select customer_id, id_tag from customer_id_tag where customer_id in (:ids) "
                        + "and country_code = current_setting('app.country')")
                .param("ids", customerIds)
                .query((rs, row) -> tags.put(rs.getObject("customer_id", UUID.class), rs.getString("id_tag")))
                .list();
        return tags;
    }
}
