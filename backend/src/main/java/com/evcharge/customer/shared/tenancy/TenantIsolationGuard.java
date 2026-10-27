package com.evcharge.customer.shared.tenancy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
class TenantIsolationGuard implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TenantIsolationGuard.class);

    private final JdbcClient jdbc;
    private final TenancySettings settings;

    TenantIsolationGuard(JdbcClient jdbc, TenancySettings settings) {
        this.jdbc = jdbc;
        this.settings = settings;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean bypasses = jdbc.sql("""
                select (select rolsuper or rolbypassrls from pg_roles where rolname = current_user)
                    or coalesce((select pg_has_role(current_user, tableowner, 'USAGE') from pg_tables
                                 where schemaname = 'public' and tablename = 'customer'), false)
                """).query(Boolean.class).single();
        if (!bypasses) {
            return;
        }
        if (settings.requireRestrictedRole()) {
            throw new IllegalStateException("The database role bypasses row-level security (superuser, BYPASSRLS or table owner): "
                    + "tenant isolation would not be enforced. Connect as a member of zella_app.");
        }
        log.warn("The database role bypasses row-level security: tenant isolation is NOT enforced by the database");
    }
}
