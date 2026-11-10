package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.application.port.StartAttempts;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
class JdbcStartAttempts implements StartAttempts {

    private final JdbcClient jdbc;

    JdbcStartAttempts(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Set<Integer> retired(UUID sessionId) {
        Optional<Integer[]> found = jdbc.sql("select retired_transactions from charging_session where id = :id")
                .param("id", sessionId).query((rs, rowNum) -> (Integer[]) rs.getArray(1).getArray()).optional();
        Set<Integer> retired = new HashSet<>();
        found.ifPresent(values -> retired.addAll(java.util.Arrays.asList(values)));
        return retired;
    }

    @Override
    public void retire(UUID sessionId, int transactionId) {
        jdbc.sql("update charging_session set retired_transactions = array_append(retired_transactions, :tx) "
                + "where id = :id").param("tx", transactionId).param("id", sessionId).update();
    }
}
