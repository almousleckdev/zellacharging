package com.evcharge.customer;

import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@ActiveProfiles("dev")
public abstract class PostgresTestBase {

    protected static final String TENANT = "GA";
    private static final String RUNTIME_USER = "zella_runtime";
    private static final String RUNTIME_PASSWORD = "runtime";

    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16");

    static {
        POSTGRES.start();
        createRestrictedRuntimeRole();
    }

    private static void createRestrictedRuntimeRole() {
        try (Connection connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(),
                POSTGRES.getPassword()); Statement statement = connection.createStatement()) {
            statement.execute("create role zella_app nologin");
            statement.execute("create role " + RUNTIME_USER + " login password '" + RUNTIME_PASSWORD
                    + "' in role zella_app");
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", () -> RUNTIME_USER);
        registry.add("spring.datasource.password", () -> RUNTIME_PASSWORD);
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "3");
        registry.add("spring.datasource.hikari.minimum-idle", () -> "1");
        registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRES::getUsername);
        registry.add("spring.flyway.password", POSTGRES::getPassword);
        registry.add("zella.tenancy.require-restricted-role", () -> "true");
        registry.add("zella.tenancy.country-cache-ttl", () -> "0s");
        registry.add("zella.charging.sweep-interval", () -> "1h");
        registry.add("zella.fees.service-fee-percent", () -> "0");
    }

    @BeforeEach
    void bindDefaultTenant() {
        TenantContext.open(TENANT);
    }

    @AfterEach
    void releaseTenant() {
        TenantContext.clear();
    }
}
