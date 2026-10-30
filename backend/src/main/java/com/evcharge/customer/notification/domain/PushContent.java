package com.evcharge.customer.notification.domain;

import java.util.Map;

public record PushContent(String title, String body, Map<String, String> data) {
}
