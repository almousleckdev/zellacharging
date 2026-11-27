package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.CustomerSignIns;
import com.evcharge.customer.account.SignInMethod;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
class JdbcCustomerSignIns implements CustomerSignIns {

    private static final String QUERY = """
            select 'PASSWORD' from customer
            where id = :id and password_login and country_code = current_setting('app.country', true)
            union
            select provider from customer_identity
            where customer_id = :id and country_code = current_setting('app.country', true)
            """;

    private final JdbcClient jdbc;

    JdbcCustomerSignIns(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<SignInMethod> of(UUID customerId) {
        return jdbc.sql(QUERY).param("id", customerId).query(String.class).list().stream()
                .map(SignInMethod::valueOf).sorted().toList();
    }
}
