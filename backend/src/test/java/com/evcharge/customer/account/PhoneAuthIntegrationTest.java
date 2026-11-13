package com.evcharge.customer.account;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.account.application.port.VerificationSender;
import com.evcharge.customer.account.domain.CodeDelivery;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PhoneAuthIntegrationTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @MockitoBean
    VerificationSender sender;

    private static String freshPhone() {
        return "+24166" + (100000 + ThreadLocalRandom.current().nextInt(900000));
    }

    private ResultActions sendJson(String path, String json) throws Exception {
        return mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private String lastCode(CodeDelivery delivery, String target) {
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(sender, atLeastOnce()).send(eq(delivery), eq(target), code.capture());
        return code.getValue();
    }

    private String register(String phone, CodeDelivery delivery) throws Exception {
        sendJson("/v1/auth/register", """
                {"firstName":"Aline","lastName":"Obame","phone":"%s","password":"Secret123!","delivery":"%s"}
                """.formatted(phone, delivery)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.channel").value("PHONE")).andExpect(jsonPath("$.target").value(phone));
        return lastCode(delivery, phone);
    }

    @Test
    void aCustomerRegistersWithAPhoneVerifiesItWithTheCodeAndSignsInWithIt() throws Exception {
        String phone = freshPhone();
        String code = register(phone, CodeDelivery.SMS);

        sendJson("/v1/auth/login", "{\"identifier\":\"" + phone + "\",\"password\":\"Secret123!\"}")
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("ACCOUNT_NOT_VERIFIED"));
        sendJson("/v1/auth/verify", "{\"channel\":\"PHONE\",\"target\":\"" + phone + "\",\"code\":\"" + code + "\"}")
                .andExpect(status().isNoContent());

        sendJson("/v1/auth/login", "{\"identifier\":\"" + phone + "\",\"password\":\"Secret123!\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void theCodeOfAPhoneRegistrationCanBeSentAgainByWhatsApp() throws Exception {
        String phone = freshPhone();
        register(phone, CodeDelivery.SMS);
        jdbc.update("update verification_code set created_at = created_at - interval '1 hour'");

        sendJson("/v1/auth/verify/resend", "{\"channel\":\"PHONE\",\"target\":\"" + phone
                + "\",\"delivery\":\"WHATSAPP\"}").andExpect(status().isAccepted());

        verify(sender, atLeastOnce()).send(eq(CodeDelivery.WHATSAPP), eq(phone), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void aForgottenPasswordIsResetWithTheCodeSentToThePhone() throws Exception {
        String phone = freshPhone();
        String code = register(phone, CodeDelivery.WHATSAPP);
        sendJson("/v1/auth/verify", "{\"channel\":\"PHONE\",\"target\":\"" + phone + "\",\"code\":\"" + code + "\"}")
                .andExpect(status().isNoContent());

        jdbc.update("update verification_code set created_at = created_at - interval '1 hour'");
        sendJson("/v1/auth/password/forgot", "{\"identifier\":\"" + phone + "\",\"delivery\":\"SMS\"}")
                .andExpect(status().isNoContent());
        String resetCode = lastCode(CodeDelivery.SMS, phone);
        sendJson("/v1/auth/password/reset", "{\"identifier\":\"" + phone + "\",\"code\":\"" + resetCode
                + "\",\"newPassword\":\"Another123!\"}").andExpect(status().isNoContent());

        sendJson("/v1/auth/login", "{\"identifier\":\"" + phone + "\",\"password\":\"Another123!\"}")
                .andExpect(status().isOk());
        assertThat(resetCode).matches("\\d{6}");
    }
}
