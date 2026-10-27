package com.evcharge.customer.admin.infrastructure;

import com.evcharge.customer.admin.application.AdminAudit;
import com.evcharge.customer.shared.Timestamps;
import org.slf4j.MDC;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

@Component
class JdbcAdminAudit implements AdminAudit {

    private final JdbcClient jdbc;
    private final Clock clock;

    JdbcAdminAudit(JdbcClient jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Override
    public void record(UUID adminId, String country, String action, String subject, String detail) {
        jdbc.sql("insert into admin_audit_record (id, admin_id, country_code, action, subject, detail, "
                        + "correlation_id, created_at) values (:id, :admin, :country, :action, :subject, :detail, "
                        + ":correlation, :now)")
                .param("id", UUID.randomUUID()).param("admin", adminId).param("country", country)
                .param("action", action).param("subject", subject).param("detail", detail)
                .param("correlation", MDC.get("correlationId")).param("now", Timestamps.toDb(clock.instant()))
                .update();
    }
}
