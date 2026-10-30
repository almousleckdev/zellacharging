package com.evcharge.customer.notification.infrastructure;

import com.evcharge.customer.notification.application.port.NotificationOutbox;
import com.evcharge.customer.notification.domain.PendingNotification;
import com.evcharge.customer.shared.NotificationType;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
class JdbcNotificationOutbox implements NotificationOutbox {

    private static final Duration LEASE = Duration.ofMinutes(2);

    private final JdbcClient jdbc;
    private final Clock clock;

    JdbcNotificationOutbox(JdbcClient jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Override
    public void enqueue(UUID customerId, NotificationType type, String dedupeKey, Map<String, String> params) {
        Object now = Timestamps.toDb(clock.instant());
        jdbc.sql("insert into notification_outbox (id, customer_id, type, params, dedupe_key, state, "
                        + "next_attempt_at, created_at) values (:id, :customer, :type, :params, :key, 'PENDING', "
                        + ":now, :now) on conflict (customer_id, dedupe_key) do nothing")
                .param("id", UUID.randomUUID()).param("customer", customerId).param("type", type.name())
                .param("params", ParamCodec.encode(params)).param("key", dedupeKey).param("now", now).update();
    }

    @Override
    public List<PendingNotification> claimDue(Instant now, int limit) {
        return jdbc.sql("update notification_outbox set next_attempt_at = :lease where id in ("
                        + "select id from notification_outbox where state = 'PENDING' "
                        + "and country_code = current_setting('app.country', true) and next_attempt_at <= :now "
                        + "order by next_attempt_at limit :limit for update skip locked) "
                        + "returning id, customer_id, type, params, attempts")
                .param("lease", Timestamps.toDb(now.plus(LEASE))).param("now", Timestamps.toDb(now))
                .param("limit", limit)
                .query((rs, row) -> new PendingNotification(rs.getObject("id", UUID.class),
                        rs.getObject("customer_id", UUID.class), NotificationType.valueOf(rs.getString("type")),
                        ParamCodec.decode(rs.getString("params")), rs.getInt("attempts")))
                .list();
    }

    @Override
    public void markSent(UUID id, Instant now, int delivered) {
        jdbc.sql("update notification_outbox set state = 'SENT', sent_at = :now, delivered_devices = :delivered, "
                        + "attempts = attempts + 1 where id = :id")
                .param("now", Timestamps.toDb(now)).param("delivered", delivered).param("id", id).update();
    }

    @Override
    public void markRetry(UUID id, Instant nextAttempt) {
        jdbc.sql("update notification_outbox set attempts = attempts + 1, next_attempt_at = :next where id = :id")
                .param("next", Timestamps.toDb(nextAttempt)).param("id", id).update();
    }

    @Override
    public void markDead(UUID id, Instant now) {
        jdbc.sql("update notification_outbox set state = 'DEAD', sent_at = :now, attempts = attempts + 1 "
                        + "where id = :id")
                .param("now", Timestamps.toDb(now)).param("id", id).update();
    }

    @Override
    public int purgeFinishedBefore(Instant cutoff) {
        return jdbc.sql("delete from notification_outbox where state <> 'PENDING' and sent_at < :cutoff "
                        + "and country_code = current_setting('app.country', true)")
                .param("cutoff", Timestamps.toDb(cutoff)).update();
    }
}
