package com.evcharge.customer.notification.application;

import com.evcharge.customer.notification.application.port.NotificationOutbox;
import com.evcharge.customer.shared.NotificationType;
import com.evcharge.customer.shared.Notifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
class NotificationService implements Notifier {

    private final NotificationOutbox outbox;

    NotificationService(NotificationOutbox outbox) {
        this.outbox = outbox;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void notify(UUID customerId, NotificationType type, String dedupeKey, Map<String, String> params) {
        outbox.enqueue(customerId, type, dedupeKey, params);
    }
}
