package com.evcharge.customer.notification.domain;

import java.time.Instant;
import java.util.UUID;

public record InboxEntry(UUID id, String type, String title, String body, String screen, String entityId,
                         Instant createdAt, boolean read) {
}
