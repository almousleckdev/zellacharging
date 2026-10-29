package com.evcharge.customer.admin.infrastructure;

import com.evcharge.customer.shared.HealthProbe;
import com.evcharge.customer.shared.ProbeResult;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
class DatabaseHealthProbe implements HealthProbe {

    private final JdbcClient jdbc;

    DatabaseHealthProbe(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String name() {
        return "database";
    }

    @Override
    public ProbeResult check() {
        long started = System.nanoTime();
        try {
            jdbc.sql("select 1").query(Integer.class).single();
            return new ProbeResult(true, (System.nanoTime() - started) / 1_000_000, null);
        } catch (RuntimeException failure) {
            return new ProbeResult(false, (System.nanoTime() - started) / 1_000_000,
                    failure.getClass().getSimpleName());
        }
    }
}
