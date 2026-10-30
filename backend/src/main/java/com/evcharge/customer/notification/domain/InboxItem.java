package com.evcharge.customer.notification.domain;

import com.evcharge.customer.shared.NotificationType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record InboxItem(UUID id, NotificationType type, Map<String, String> params, Instant createdAt,
                        boolean read) {
}
