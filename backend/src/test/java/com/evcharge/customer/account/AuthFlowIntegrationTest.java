package com.evcharge.customer.account;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.account.application.port.VerificationSender;
import com.evcharge.customer.account.domain.Channel;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    VerificationSender sender;

    private ResultActions postJson(String path, String json) throws Exception {
        return mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private String lastCode(Channel channel, String target) {
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(sender, atLeastOnce()).send(eq(channel), eq(target), code.capture());
        return code.getValue();
    }

    private void registerAndVerifyEmail(String email) throws Exception {
        postJson("/v1/auth/register", """
                {"firstName":"Aline","lastName":"Obame","email":"%s","password":"Secret123!"}
                """.formatted(email)).andExpect(status().isCreated());
        postJson("/v1/auth/verify", """
                {"channel":"EMAIL","target":"%s","code":"%s"}
                """.formatted(email, lastCode(Channel.EMAIL, email))).andExpect(status().isNoContent());
    }

    @Test
    void fullLifecycleWithRefreshRotationAndReuseDetection() throws Exception {
        String email = "aline@example.com";
        postJson("/v1/auth/register", """
                {"firstName":"Aline","lastName":"Obame","email":"%s","password":"Secret123!"}
                """.formatted(email)).andExpect(status().isCreated());
        postJson("/v1/auth/login", """
                {"identifier":"%s","password":"Secret123!"}
                """.formatted(email)).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_VERIFIED"));
        postJson("/v1/auth/verify", """
                {"channel":"EMAIL","target":"%s","code":"%s"}
                """.formatted(email, lastCode(Channel.EMAIL, email))).andExpect(status().isNoContent());

        String body = postJson("/v1/auth/login", """
                {"identifier":"%s","password":"Secret123!"}
                """.formatted(email)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String access = JsonPath.read(body, "$.accessToken");
        String refresh = JsonPath.read(body, "$.refreshToken");

        mvc.perform(get("/v1/me").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        mvc.perform(get("/v1/me")).andExpect(status().isUnauthorized());

        String rotated = postJson("/v1/auth/refresh", "{\"refreshToken\":\"%s\"}".formatted(refresh))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String newRefresh = JsonPath.read(rotated, "$.refreshToken");
        assertThat(newRefresh).isNotEqualTo(refresh);

        postJson("/v1/auth/refresh", "{\"refreshToken\":\"%s\"}".formatted(refresh)).andExpect(status().isUnauthorized());
        postJson("/v1/auth/refresh", "{\"refreshToken\":\"%s\"}".formatted(newRefresh)).andExpect(status().isUnauthorized());
    }

    @Test
    void phoneRegistrationUsesSixDigitCodeAndNormalizesNumber() throws Exception {
        postJson("/v1/auth/register", """
                {"firstName":"Jean","lastName":"Mba","phone":"123","password":"Secret123!"}
                """).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PHONE"));
        postJson("/v1/auth/register", """
                {"firstName":"Jean","lastName":"Mba","phone":"+24177123456","password":"Secret123!"}
                """).andExpect(status().isCreated());
        String code = lastCode(Channel.PHONE, "+24177123456");
        assertThat(code).matches("\\d{6}");
        postJson("/v1/auth/verify", """
                {"channel":"PHONE","target":"077123456","code":"000000x"}
                """).andExpect(status().isBadRequest());
    }

    @Test
    void registrationRequiresContactAndRejectsDuplicates() throws Exception {
        postJson("/v1/auth/register", """
                {"firstName":"No","lastName":"Contact","password":"Secret123!"}
                """).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("CONTACT_REQUIRED"));
        registerAndVerifyEmail("dup@example.com");
        postJson("/v1/auth/register", """
                {"firstName":"B","lastName":"B","email":"DUP@example.com","password":"Secret123!"}
                """).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("CONTACT_ALREADY_USED"));
    }

    @Test
    void wrongCodeIsRejectedAndAccountLocksAfterRepeatedBadPasswords() throws Exception {
        String email = "lock@example.com";
        registerAndVerifyEmail(email);
        for (int i = 0; i < 5; i++) {
            postJson("/v1/auth/login", """
                    {"identifier":"%s","password":"wrong-password"}
                    """.formatted(email)).andExpect(status().isUnauthorized());
        }
        postJson("/v1/auth/login", """
                {"identifier":"%s","password":"Secret123!"}
                """.formatted(email)).andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("ACCOUNT_LOCKED"));
    }
}
