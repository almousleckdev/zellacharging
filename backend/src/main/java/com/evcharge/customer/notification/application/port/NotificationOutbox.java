package com.evcharge.customer.notification.application.port;

import com.evcharge.customer.notification.domain.PendingNotification;
import com.evcharge.customer.shared.NotificationType;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface NotificationOutbox {

    void enqueue(UUID customerId, NotificationType type, String dedupeKey, Map<String, String> params);

    List<PendingNotification> claimDue(Instant now, int limit);

    void markSent(UUID id, Instant now, int delivered);

    void markRetry(UUID id, Instant nextAttempt);

    void markDead(UUID id, Instant now);

    int purgeFinishedBefore(Instant cutoff);
}
