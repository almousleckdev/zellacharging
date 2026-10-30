package com.evcharge.customer.notification.domain;

import java.util.Map;

public record PushContent(String title, String body, Map<String, String> data, int badge) {

    public PushContent withBadge(int unread) {
        return new PushContent(title, body, data, unread);
    }
}
