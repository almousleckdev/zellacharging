package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.application.AccountSettings;
import com.evcharge.customer.account.application.SupportSettings;
import com.evcharge.customer.account.application.port.ContactInbox;
import com.evcharge.customer.account.domain.ContactMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
class EmailContactInbox implements ContactInbox {

    private static final Logger log = LoggerFactory.getLogger(EmailContactInbox.class);

    private final JavaMailSender mail;
    private final AccountSettings account;
    private final SupportSettings support;

    EmailContactInbox(JavaMailSender mail, AccountSettings account, SupportSettings support) {
        this.mail = mail;
        this.account = account;
        this.support = support;
    }

    @Override
    public boolean deliver(ContactMessage message) {
        if (support.contactInbox() == null || support.contactInbox().isBlank()) {
            log.warn("Contact message dropped: zella.support.contact-inbox is not set");
            return false;
        }
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(account.mailFrom());
        mailMessage.setTo(support.contactInbox());
        mailMessage.setReplyTo(message.email());
        mailMessage.setSubject("[Website] " + message.topic() + " - " + message.name());
        mailMessage.setText("From: %s <%s>%nPhone: %s%nTopic: %s%n%n%s".formatted(message.name(), message.email(),
                message.phone() == null ? "-" : message.phone(), message.topic(), message.message()));
        try {
            mail.send(mailMessage);
            return true;
        } catch (MailException e) {
            log.warn("Contact message could not be sent: {}", e.getMessage());
            return false;
        }
    }
}
