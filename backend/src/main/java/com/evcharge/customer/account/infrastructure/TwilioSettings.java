package com.evcharge.customer.account.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("zella.twilio")
public record TwilioSettings(String account, String key, String secret, String service, String template,
                             String smsService, String smsNumber, String baseUrl) {

    public TwilioSettings {
        baseUrl = baseUrl == null || baseUrl.isBlank() ? "https://api.twilio.com" : baseUrl;
    }

    boolean whatsAppConfigured() {
        return credentialsFilled() && filled(service) && filled(template);
    }

    boolean smsConfigured() {
        return credentialsFilled() && (filled(smsService) || filled(smsNumber));
    }

    private boolean credentialsFilled() {
        return filled(account) && filled(key) && filled(secret);
    }

    private static boolean filled(String value) {
        return value != null && !value.isBlank();
    }
}
