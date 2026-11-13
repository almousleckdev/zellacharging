package com.evcharge.customer.account;

import com.evcharge.customer.FakeOcppServer;
import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.account.application.port.VerificationSender;
import com.evcharge.customer.account.domain.CodeDelivery;
import com.evcharge.customer.shared.ApiException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class WhatsAppCodeDeliveryTest extends PostgresTestBase {

    private static final String MESSAGES = "/2010-04-01/Accounts/AC123/Messages.json";

    private static final FakeOcppServer TWILIO = new FakeOcppServer().route("POST", MESSAGES, 201, "{\"sid\":\"SM1\"}");

    @DynamicPropertySource
    static void twilio(DynamicPropertyRegistry registry) {
        registry.add("zella.twilio.base-url", TWILIO::baseUrl);
        registry.add("zella.twilio.account", () -> "AC123");
        registry.add("zella.twilio.key", () -> "SK123");
        registry.add("zella.twilio.secret", () -> "secret");
        registry.add("zella.twilio.service", () -> "MG123");
        registry.add("zella.twilio.template", () -> "HX123");
        registry.add("zella.twilio.sms-service", () -> "MG456");
    }

    @AfterAll
    static void stop() {
        TWILIO.stop();
    }

    @Autowired
    VerificationSender sender;

    @BeforeEach
    void freshCount() {
        TWILIO.resetHits();
    }

    @Test
    void aPhoneCodeIsSentThroughTwilioWhatsApp() {
        sender.send(CodeDelivery.WHATSAPP, "+24177000001", "123456");

        assertThat(TWILIO.hits("POST", MESSAGES)).isEqualTo(1);
    }

    @Test
    void aPhoneCodeCanAlsoBeSentBySms() {
        sender.send(CodeDelivery.SMS, "+24177000003", "123456");

        assertThat(TWILIO.hits("POST", MESSAGES)).isEqualTo(1);
    }

    @Test
    void whenTwilioRefusesTheCustomerIsToldTheCodeWasNotSent() {
        TWILIO.route("POST", MESSAGES, 400, "{\"code\":21211}");

        assertThatThrownBy(() -> sender.send(CodeDelivery.WHATSAPP, "+24177000002", "123456"))
                .isInstanceOf(ApiException.class).hasMessageContaining("could not be sent");
        TWILIO.route("POST", MESSAGES, 201, "{\"sid\":\"SM1\"}");
    }
}
