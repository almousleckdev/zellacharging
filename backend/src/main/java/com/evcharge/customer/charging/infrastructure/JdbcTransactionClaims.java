package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.application.port.TransactionClaims;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
class JdbcTransactionClaims implements TransactionClaims {

    private final JdbcClient jdbc;

    JdbcTransactionClaims(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean heldByAnother(int transactionId, UUID sessionId) {
        return jdbc.sql("select exists (select 1 from charging_session where transaction_id = :tx and id <> :id)")
                .param("tx", transactionId).param("id", sessionId).query(Boolean.class).single();
    }

    @Override
    public boolean knownIdTag(String idTag) {
        return jdbc.sql("""
                select exists (select 1 from customer_id_tag where id_tag = :tag)
                    or exists (select 1 from charging_session where id_tag = :tag)
                """).param("tag", idTag).query(Boolean.class).single();
    }
}
