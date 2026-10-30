package com.evcharge.customer.notification.infrastructure;

import com.evcharge.customer.notification.application.port.NotificationInbox;
import com.evcharge.customer.notification.domain.InboxItem;
import com.evcharge.customer.shared.NotificationType;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
class JdbcNotificationInbox implements NotificationInbox {

    private static final String OWN = "customer_id = :customer and country_code = current_setting('app.country', true)";

    private final JdbcClient jdbc;

    JdbcNotificationInbox(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<InboxItem> recent(UUID customerId, int limit) {
        return jdbc.sql("select id, type, params, created_at, read_at from notification_outbox where " + OWN
                        + " order by created_at desc, id limit :limit")
                .param("customer", customerId).param("limit", limit)
                .query((rs, row) -> new InboxItem(rs.getObject("id", UUID.class),
                        NotificationType.valueOf(rs.getString("type")), ParamCodec.decode(rs.getString("params")),
                        Timestamps.read(rs, "created_at"), rs.getObject("read_at") != null))
                .list();
    }

    @Override
    public int unread(UUID customerId) {
        return jdbc.sql("select count(*) from notification_outbox where " + OWN + " and read_at is null")
                .param("customer", customerId).query(Integer.class).single();
    }

    @Override
    public void markRead(UUID customerId, UUID id, Instant now) {
        jdbc.sql("update notification_outbox set read_at = :now where id = :id and read_at is null and " + OWN)
                .param("now", Timestamps.toDb(now)).param("id", id).param("customer", customerId).update();
    }

    @Override
    public void markAllRead(UUID customerId, Instant now) {
        jdbc.sql("update notification_outbox set read_at = :now where read_at is null and " + OWN)
                .param("now", Timestamps.toDb(now)).param("customer", customerId).update();
    }
}
