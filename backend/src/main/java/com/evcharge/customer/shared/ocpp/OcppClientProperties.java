package com.evcharge.customer.shared.ocpp;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ndzela.ocpp")
public record OcppClientProperties(String baseUrl, String apiKey, String signingSecret) {

    public boolean signingEnabled() {
        return signingSecret != null && !signingSecret.isBlank();
    }
}
