package com.evcharge.customer.notification.application.port;

import com.evcharge.customer.notification.domain.InboxItem;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NotificationInbox {

    List<InboxItem> recent(UUID customerId, int limit);

    int unread(UUID customerId);

    void markRead(UUID customerId, UUID id, Instant now);

    void markAllRead(UUID customerId, Instant now);
}
