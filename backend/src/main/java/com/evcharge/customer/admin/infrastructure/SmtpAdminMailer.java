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
        return deliver(email, "Zella Charging : invitation à rejoindre la console / console invitation",
                "Bonjour,\n\nVous avez été invité(e) à rejoindre la console d'administration Zella Charging.\n"
                + "Créez votre mot de passe, activez l'authentification à deux facteurs puis complétez votre profil :\n\n"
                + link + "\n\nCe lien est valable " + hours + " heures et ne peut être utilisé qu'une fois.\n\n"
                + "---\n\nHello,\n\nYou have been invited to join the Zella Charging admin console.\n"
                + "Set your password, enable two-factor authentication, then complete your profile:\n\n"
                + link + "\n\nThis link is valid for " + hours + " hours and can only be used once.\n");
    }

    @Override
    public boolean sendPasswordReset(String email, String link, Instant expiresAt) {
        long minutes = Math.max(1, Duration.between(Instant.now(), expiresAt).toMinutes());
        return deliver(email, "Zella Charging : réinitialisation du mot de passe / password reset",
                "Bonjour,\n\nUne réinitialisation du mot de passe de votre compte Zella Charging a été demandée.\n"
                + "Choisissez un nouveau mot de passe :\n\n"
                + link + "\n\nCe lien est valable " + minutes + " minutes et ne peut être utilisé qu'une fois.\n"
                + "Si vous n'êtes pas à l'origine de cette demande, ignorez ce message.\n\n"
                + "---\n\nHello,\n\nA password reset was requested for your Zella Charging account.\n"
                + "Choose a new password:\n\n"
                + link + "\n\nThis link is valid for " + minutes + " minutes and can only be used once.\n"
                + "If you did not request this, ignore this message.\n");
    }

    private boolean deliver(String email, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(settings.mailFrom());
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);
        try {
            mail.send(message);
            return true;
        } catch (MailException e) {
            log.warn("Admin email could not be sent: {}", e.getMessage());
            return false;
        }
    }
}
