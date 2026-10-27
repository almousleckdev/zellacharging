package com.evcharge.customer.shared.audit;

import com.evcharge.customer.shared.AuditTrail;
import com.evcharge.customer.shared.Timestamps;
import org.slf4j.MDC;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

@Component
class JdbcAuditTrail implements AuditTrail {

    private final JdbcClient jdbc;
    private final Clock clock;

    JdbcAuditTrail(JdbcClient jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Override
    public void record(String action, UUID actorId, String subject, String detail) {
        jdbc.sql("insert into audit_record (id, actor_id, action, subject, detail, correlation_id, created_at) "
                        + "values (:id, :actor, :action, :subject, :detail, :correlation, :now)")
                .param("id", UUID.randomUUID()).param("actor", actorId).param("action", action)
                .param("subject", subject).param("detail", detail).param("correlation", MDC.get("correlationId"))
                .param("now", Timestamps.toDb(clock.instant())).update();
    }
}
