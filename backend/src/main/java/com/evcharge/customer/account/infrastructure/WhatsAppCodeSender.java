package com.evcharge.customer.account.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
class WhatsAppCodeSender {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppCodeSender.class);

    private final TwilioSettings settings;
    private final TwilioMessages twilio;

    WhatsAppCodeSender(TwilioSettings settings, TwilioMessages twilio) {
        this.settings = settings;
        this.twilio = twilio;
    }

    void send(String phoneE164, String code) {
        if (!settings.whatsAppConfigured()) {
            log.warn("Twilio WhatsApp is not configured: code for {} is {}", phoneE164, code);
            return;
        }
        Map<String, String> form = new LinkedHashMap<>();
        form.put("To", "whatsapp:" + phoneE164);
        form.put("MessagingServiceSid", settings.service());
        form.put("ContentSid", settings.templateFor(LocaleContextHolder.getLocale()));
        form.put("ContentVariables", "{\"1\":\"" + code + "\"}");
        twilio.post(phoneE164, form);
    }
}
