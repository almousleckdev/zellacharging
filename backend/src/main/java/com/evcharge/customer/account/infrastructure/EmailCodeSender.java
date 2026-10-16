package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.application.AccountSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
class EmailCodeSender {

    private static final Logger log = LoggerFactory.getLogger(EmailCodeSender.class);

    private final JavaMailSender mail;
    private final AccountSettings settings;

    EmailCodeSender(JavaMailSender mail, AccountSettings settings) {
        this.mail = mail;
        this.settings = settings;
    }

    void send(String address, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(settings.mailFrom());
        message.setTo(address);
        message.setSubject("Ndzela - verification code");
        message.setText("Your Ndzela verification code is " + code + ". It expires in "
                + settings.verification().codeTtl().toMinutes() + " minutes.");
        try {
            mail.send(message);
        } catch (MailException e) {
            log.warn("Verification email could not be sent: {}", e.getMessage());
        }
    }
}
