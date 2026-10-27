package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.application.AccountSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
class EmailCodeSender {

    private static final Logger log = LoggerFactory.getLogger(EmailCodeSender.class);

    private final JavaMailSender mail;
    private final MessageSource messages;
    private final AccountSettings settings;

    EmailCodeSender(JavaMailSender mail, MessageSource messages, AccountSettings settings) {
        this.mail = mail;
        this.messages = messages;
        this.settings = settings;
    }

    void send(String address, String code) {
        Locale locale = LocaleContextHolder.getLocale();
        Object[] arguments = {code, settings.verification().codeTtl().toMinutes()};
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(settings.mailFrom());
        message.setTo(address);
        message.setSubject(messages.getMessage("email.verification.subject", null, locale));
        message.setText(messages.getMessage("email.verification.body", arguments, locale));
        try {
            mail.send(message);
        } catch (MailException e) {
            log.warn("Verification email could not be sent: {}", e.getMessage());
        }
    }
}
