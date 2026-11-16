package com.evcharge.customer.partner.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("zella.ndzela")
public record NdzelaSettings(String baseUrl, String apiKey) {

    boolean configured() {
        return filled(baseUrl) && filled(apiKey);
    }

    private static boolean filled(String value) {
        return value != null && !value.isBlank();
    }
}
