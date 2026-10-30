package com.evcharge.customer.notification.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("zella.notifications.fcm")
public record FcmSettings(String credentialsJson, String androidChannel) {

    boolean configured() {
        return credentialsJson != null && !credentialsJson.isBlank();
    }
}
