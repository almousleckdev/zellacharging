package com.evcharge.customer.shared;

import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.Instant;
import java.util.UUID;

public record LogFilter(Instant from, Instant to, UUID customerId) {

    public static final LogFilter NONE = new LogFilter(null, null, null);

    public String clause(String timeColumn, String customerColumn) {
        return (from == null ? "" : " and " + timeColumn + " >= :filterFrom")
                + (to == null ? "" : " and " + timeColumn + " < :filterTo")
                + (customerId == null ? "" : " and " + customerColumn + " = :filterCustomer");
    }

    public JdbcClient.StatementSpec bind(JdbcClient.StatementSpec spec) {
        if (from != null) {
            spec = spec.param("filterFrom", Timestamps.toDb(from));
        }
        if (to != null) {
            spec = spec.param("filterTo", Timestamps.toDb(to));
        }
        if (customerId != null) {
            spec = spec.param("filterCustomer", customerId);
        }
        return spec;
    }
}
