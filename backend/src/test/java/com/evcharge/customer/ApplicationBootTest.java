package com.evcharge.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplicationBootTest extends PostgresTestBase {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void flywayAppliedAllMigrations() {
        Integer applied = jdbc.queryForObject(
                "select count(*) from flyway_schema_history where success", Integer.class);
        assertThat(applied).isEqualTo(8);
    }
}
