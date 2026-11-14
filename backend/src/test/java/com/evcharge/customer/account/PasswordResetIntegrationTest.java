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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PasswordResetIntegrationTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    VerificationSender sender;

    private ResultActions postJson(String path, String json) throws Exception {
        return mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private String lastCode(String email) {
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(sender, atLeastOnce()).send(eq(CodeDelivery.EMAIL), eq(email), code.capture());
        return code.getValue();
    }

    private static String phoneFor(String email) {
        return "+24177" + String.format("%06d", Math.abs(email.hashCode()) % 1000000);
    }

    private String verifiedAccount() throws Exception {
        String email = "reset-" + UUID.randomUUID() + "@example.com";
        postJson("/v1/auth/register", """
                {"firstName":"Aline","lastName":"Obame","email":"%s","phone":"%s","password":"OldSecret123!"}
                """.formatted(email, phoneFor(email))).andExpect(status().isCreated());
        postJson("/v1/auth/verify", """
                {"channel":"EMAIL","target":"%s","code":"%s"}
                """.formatted(email, lastCode(email))).andExpect(status().isNoContent());
        return email;
    }

    private ResultActions login(String email, String password) throws Exception {
        return postJson("/v1/auth/login", """
                {"identifier":"%s","password":"%s"}
                """.formatted(email, password));
    }

    @Test
    void aCustomerResetsTheirPasswordWithTheCodeSentByEmail() throws Exception {
        String email = verifiedAccount();
        postJson("/v1/auth/password/forgot", "{\"email\":\"" + email + "\"}").andExpect(status().isNoContent());

        postJson("/v1/auth/password/reset", """
                {"email":"%s","code":"%s","newPassword":"NewSecret456!"}
                """.formatted(email, lastCode(email))).andExpect(status().isNoContent());

        login(email, "OldSecret123!").andExpect(status().isUnauthorized());
        login(email, "NewSecret456!").andExpect(status().isOk());
    }

    @Test
    void aWrongCodeNeverChangesThePassword() throws Exception {
        String email = verifiedAccount();
        postJson("/v1/auth/password/forgot", "{\"email\":\"" + email + "\"}").andExpect(status().isNoContent());

        postJson("/v1/auth/password/reset", """
                {"email":"%s","code":"000000","newPassword":"NewSecret456!"}
                """.formatted(email)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CODE"));

        login(email, "OldSecret123!").andExpect(status().isOk());
    }

    @Test
    void anUnknownEmailGetsTheSameAnswerAndNoMessage() throws Exception {
        String stranger = "nobody-" + UUID.randomUUID() + "@example.com";

        postJson("/v1/auth/password/forgot", "{\"email\":\"" + stranger + "\"}").andExpect(status().isNoContent());
        postJson("/v1/auth/password/reset", """
                {"email":"%s","code":"123456","newPassword":"NewSecret456!"}
                """.formatted(stranger)).andExpect(status().isBadRequest());

        verify(sender, never()).send(eq(CodeDelivery.EMAIL), eq(stranger), anyString());
    }

    @Test
    void anAccountWhoseEmailIsNotVerifiedGetsNoResetCode() throws Exception {
        String email = "pending-" + UUID.randomUUID() + "@example.com";
        postJson("/v1/auth/register", """
                {"firstName":"Aline","lastName":"Obame","email":"%s","phone":"%s","password":"OldSecret123!"}
                """.formatted(email, phoneFor(email))).andExpect(status().isCreated());
        String registrationCode = lastCode(email);

        postJson("/v1/auth/password/forgot", "{\"email\":\"" + email + "\"}").andExpect(status().isNoContent());
        postJson("/v1/auth/password/reset", """
                {"email":"%s","code":"%s","newPassword":"NewSecret456!"}
                """.formatted(email, registrationCode)).andExpect(status().isBadRequest());
    }

    @Test
    void aVerificationCodeCannotResetAPassword() throws Exception {
        String email = verifiedAccount();
        postJson("/v1/auth/password/forgot", "{\"email\":\"" + email + "\"}").andExpect(status().isNoContent());
        String resetCode = lastCode(email);

        postJson("/v1/auth/verify", """
                {"channel":"EMAIL","target":"%s","code":"%s"}
                """.formatted(email, resetCode)).andExpect(status().isBadRequest());
    }

    @Test
    void aTooShortNewPasswordIsRefused() throws Exception {
        postJson("/v1/auth/password/reset", """
                {"email":"a@example.com","code":"123456","newPassword":"short"}
                """).andExpect(status().isBadRequest());
    }
}
