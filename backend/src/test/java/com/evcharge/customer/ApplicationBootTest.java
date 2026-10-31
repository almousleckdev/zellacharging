package com.evcharge.customer;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplicationBootTest extends PostgresTestBase {

    @Autowired
    Flyway flyway;

    @Test
    void flywayAppliedAllMigrations() {
        assertThat(flyway.info().applied()).hasSize(27);
        assertThat(flyway.info().pending()).isEmpty();
    }
}
