package com.evcharge.customer.shared.ocpp;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("zella.ocpp")
public record OcppClientProperties(String baseUrl, String apiKey, String signingSecret,
                                   boolean requireSecureChannel, int requestsPerMinute,
                                   int priorityReserve) {

    public boolean signingEnabled() {
        return signingSecret != null && !signingSecret.isBlank();
    }
}
