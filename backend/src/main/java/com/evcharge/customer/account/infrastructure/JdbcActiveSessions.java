package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.shared.ActiveSessions;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
class JdbcActiveSessions implements ActiveSessions {

    private final JdbcClient jdbc;

    JdbcActiveSessions(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean isActive(UUID sessionId) {
        return jdbc.sql("select exists (select 1 from refresh_token where family_id = :session "
                        + "and revoked_at is null)")
                .param("session", sessionId).query(Boolean.class).single();
    }
}
