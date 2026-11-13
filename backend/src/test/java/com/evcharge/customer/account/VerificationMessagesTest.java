package com.evcharge.customer.account;

import com.evcharge.customer.PostgresTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class VerificationMessagesTest extends PostgresTestBase {

    @Autowired
    MessageSource messages;

    private String sms(Locale locale) {
        return messages.getMessage("sms.verification.body", new Object[]{"123456", 10}, locale);
    }

    @Test
    void theSmsIsInFrenchByDefaultAndForFrenchCustomers() {
        assertThat(sms(Locale.FRENCH)).isEqualTo(
                "Zella Charging : votre code de vérification est 123456. Il expire dans 10 minutes.");
        assertThat(sms(Locale.ROOT)).startsWith("Zella Charging : votre code");
    }

    @Test
    void theSmsIsInEnglishOnlyForEnglishCustomers() {
        assertThat(sms(Locale.ENGLISH)).isEqualTo(
                "Zella Charging: your verification code is 123456. It expires in 10 minutes.");
    }
}
