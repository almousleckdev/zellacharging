package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.application.AccountSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
class SmsCodeSender {

    private static final Logger log = LoggerFactory.getLogger(SmsCodeSender.class);

    private final TwilioSettings settings;
    private final TwilioMessages twilio;
    private final MessageSource messages;
    private final AccountSettings account;

    SmsCodeSender(TwilioSettings settings, TwilioMessages twilio, MessageSource messages, AccountSettings account) {
        this.settings = settings;
        this.twilio = twilio;
        this.messages = messages;
        this.account = account;
    }

    void send(String phoneE164, String code) {
        if (!settings.smsConfigured()) {
            log.warn("Twilio SMS is not configured: code for {} is {}", phoneE164, code);
            return;
        }
        Object[] arguments = {code, account.verification().codeTtl().toMinutes()};
        Map<String, String> form = new LinkedHashMap<>();
        form.put("To", phoneE164);
        form.put("Body", messages.getMessage("sms.verification.body", arguments, LocaleContextHolder.getLocale()));
        if (settings.smsService() != null && !settings.smsService().isBlank()) {
            form.put("MessagingServiceSid", settings.smsService());
        } else {
            form.put("From", settings.smsNumber());
        }
        twilio.post(phoneE164, form);
    }
}
