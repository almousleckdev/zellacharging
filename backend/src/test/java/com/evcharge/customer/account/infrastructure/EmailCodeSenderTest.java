package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.application.AccountSettings;
import com.evcharge.customer.account.application.VerificationSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Duration;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EmailCodeSenderTest {

    private final JavaMailSender mail = mock(JavaMailSender.class);
    private final EmailCodeSender sender = new EmailCodeSender(mail, messages(), settings());

    private static ResourceBundleMessageSource messages() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("errors");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        return source;
    }

    private static AccountSettings settings() {
        return new AccountSettings(null, new VerificationSettings(Duration.ofMinutes(10), Duration.ofMinutes(1), 5),
                "no-reply@zella.test");
    }

    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    private SimpleMailMessage sentIn(Locale locale) {
        LocaleContextHolder.setLocale(locale);
        sender.send("aline@example.com", "123456");
        ArgumentCaptor<SimpleMailMessage> message = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mail).send(message.capture());
        return message.getValue();
    }

    @Test
    void theCodeIsMailedInFrenchByDefault() {
        SimpleMailMessage message = sentIn(Locale.FRENCH);

        assertThat(message.getSubject()).contains("code de vérification");
        assertThat(message.getText()).contains("123456").contains("10 minutes").contains("Zella Charging");
        assertThat(message.getTo()).containsExactly("aline@example.com");
        assertThat(message.getFrom()).isEqualTo("no-reply@zella.test");
    }

    @Test
    void theCodeIsMailedInEnglishForAnEnglishRequest() {
        SimpleMailMessage message = sentIn(Locale.ENGLISH);

        assertThat(message.getSubject()).contains("verification code");
        assertThat(message.getText()).contains("123456").contains("10 minutes");
    }

    @Test
    void anUnsupportedLanguageFallsBackToFrench() {
        assertThat(sentIn(Locale.GERMAN).getSubject()).contains("code de vérification");
    }

    @Test
    void aMailFailureDoesNotBreakRegistration() {
        doThrow(new MailSendException("smtp down")).when(mail).send(any(SimpleMailMessage.class));

        assertThatCode(() -> sender.send("aline@example.com", "123456")).doesNotThrowAnyException();
    }
}
