package com.evcharge.customer.account.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Locale;

@ConfigurationProperties("zella.twilio")
public record TwilioSettings(String account, String key, String secret, String service, String template,
                             String smsService, String smsNumber, String templateEn, String baseUrl) {

    public TwilioSettings {
        baseUrl = baseUrl == null || baseUrl.isBlank() ? "https://api.twilio.com" : baseUrl;
    }

    String templateFor(Locale locale) {
        boolean english = Locale.ENGLISH.getLanguage().equals(locale.getLanguage());
        return english && filled(templateEn) ? templateEn : template;
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
