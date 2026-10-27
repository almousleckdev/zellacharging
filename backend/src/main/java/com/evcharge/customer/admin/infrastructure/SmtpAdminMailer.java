package com.evcharge.customer.admin.infrastructure;

import com.evcharge.customer.admin.application.AdminSettings;
import com.evcharge.customer.admin.application.port.AdminMailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
class SmtpAdminMailer implements AdminMailer {

    private static final Logger log = LoggerFactory.getLogger(SmtpAdminMailer.class);

    private final JavaMailSender mail;
    private final AdminSettings settings;

    SmtpAdminMailer(JavaMailSender mail, AdminSettings settings) {
        this.mail = mail;
        this.settings = settings;
    }

    @Override
    public boolean sendInvitation(String email, String link, Instant expiresAt) {
        long hours = (Duration.between(Instant.now(), expiresAt).toMinutes() + 59) / 60;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(settings.mailFrom());
        message.setTo(email);
        message.setSubject("Zella Charging : invitation à rejoindre la console / console invitation");
        message.setText("Bonjour,\n\nVous avez été invité(e) à rejoindre la console d'administration Zella Charging.\n"
                + "Créez votre mot de passe, activez l'authentification à deux facteurs puis complétez votre profil :\n\n"
                + link + "\n\nCe lien est valable " + hours + " heures et ne peut être utilisé qu'une fois.\n\n"
                + "---\n\nHello,\n\nYou have been invited to join the Zella Charging admin console.\n"
                + "Set your password, enable two-factor authentication, then complete your profile:\n\n"
                + link + "\n\nThis link is valid for " + hours + " hours and can only be used once.\n");
        try {
            mail.send(message);
            return true;
        } catch (MailException e) {
            log.warn("Invitation email could not be sent: {}", e.getMessage());
            return false;
        }
    }
}
