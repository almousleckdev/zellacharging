package com.evcharge.customer.shared;

import java.util.Map;
import java.util.UUID;

public interface Notifier {

    void notify(UUID customerId, NotificationType type, String dedupeKey, Map<String, String> params);
}
