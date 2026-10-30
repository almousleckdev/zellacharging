package com.evcharge.customer.notification.infrastructure;

import com.evcharge.customer.notification.domain.DeviceToken;
import com.evcharge.customer.notification.domain.PushContent;

import java.util.LinkedHashMap;
import java.util.Map;

final class FcmMessage {

    private FcmMessage() {
    }

    static Map<String, Object> of(DeviceToken device, PushContent content, String androidChannel) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("token", device.token());
        message.put("notification", Map.of("title", content.title(), "body", content.body()));
        message.put("data", content.data());
        message.put("android", android(content, androidChannel));
        message.put("apns", apns(content));
        return message;
    }

    private static Map<String, Object> android(PushContent content, String channel) {
        return Map.of("priority", "HIGH", "notification", Map.of("channel_id", channel, "default_sound", true,
                "visibility", "PUBLIC", "notification_count", content.badge()));
    }

    private static Map<String, Object> apns(PushContent content) {
        return Map.of("headers", Map.of("apns-priority", "10", "apns-push-type", "alert"),
                "payload", Map.of("aps", Map.of("sound", "default", "badge", content.badge())));
    }
}
