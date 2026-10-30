package com.evcharge.customer.notification.domain;

import com.evcharge.customer.shared.NotificationType;

import java.util.Map;
import java.util.UUID;

public record PendingNotification(UUID id, UUID customerId, NotificationType type, Map<String, String> params,
                                  int attempts) {
}
