package com.evcharge.customer.account;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.account.application.port.VerificationSender;
import com.evcharge.customer.account.domain.CodeDelivery;
import com.jayway.jsonpath.JsonPath;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
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

    @Autowired
    JdbcTemplate jdbc;

    @MockitoBean
    VerificationSender sender;

    private ResultActions postJson(String path, String json) throws Exception {
        return mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private String lastCode(CodeDelivery delivery, String target) {
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(sender, atLeastOnce()).send(eq(delivery), eq(target), code.capture());
        return code.getValue();
    }

    private static String phoneFor(String email) {
        return "+24177" + String.format("%06d", Math.abs(email.hashCode()) % 1000000);
    }

    private void registerAndVerifyEmail(String email) throws Exception {
        postJson("/v1/auth/register", """
                {"firstName":"Aline","lastName":"Obame","email":"%s","phone":"%s","password":"Secret123!"}
                """.formatted(email, phoneFor(email))).andExpect(status().isCreated());
        postJson("/v1/auth/verify", """
                {"channel":"EMAIL","target":"%s","code":"%s"}
                """.formatted(email, lastCode(CodeDelivery.EMAIL, email))).andExpect(status().isNoContent());
    }

    @Test
    void fullLifecycleWithRefreshRotationAndReuseDetection() throws Exception {
        String email = "aline@example.com";
        postJson("/v1/auth/register", """
                {"firstName":"Aline","lastName":"Obame","email":"%s","phone":"%s","password":"Secret123!"}
                """.formatted(email, phoneFor(email))).andExpect(status().isCreated());
        postJson("/v1/auth/login", """
                {"identifier":"%s","password":"Secret123!"}
                """.formatted(email)).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_VERIFIED"));
        postJson("/v1/auth/verify", """
                {"channel":"EMAIL","target":"%s","code":"%s"}
                """.formatted(email, lastCode(CodeDelivery.EMAIL, email))).andExpect(status().isNoContent());

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
    void registrationNeedsBothAnEmailAndAPhone() throws Exception {
        postJson("/v1/auth/register", """
                {"firstName":"No","lastName":"Contact","password":"Secret123!"}
                """).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("CONTACT_REQUIRED"));
        postJson("/v1/auth/register", """
                {"firstName":"No","lastName":"Phone","email":"nophone@example.com","password":"Secret123!"}
                """).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("CONTACT_REQUIRED"));
        postJson("/v1/auth/register", """
                {"firstName":"No","lastName":"Email","phone":"077123499","password":"Secret123!"}
                """).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("CONTACT_REQUIRED"));
        postJson("/v1/auth/register", """
                {"firstName":"No","lastName":"Email","email":"  ","phone":"077123499","password":"Secret123!"}
                """).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("CONTACT_REQUIRED"));
    }

    @Test
    void registrationSendsACodeToEachContactAndStoresThePhoneNormalized() throws Exception {
        postJson("/v1/auth/register", """
                {"firstName":"Jean","lastName":"Mba","email":"jean@example.com","phone":"123","password":"Secret123!"}
                """).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PHONE"));

        postJson("/v1/auth/register", """
                {"firstName":"Jean","lastName":"Mba","email":"jean@example.com","phone":"077123456",
                 "password":"Secret123!"}
                """).andExpect(status().isCreated()).andExpect(jsonPath("$.channel").value("PHONE"));

        assertThat(lastCode(CodeDelivery.EMAIL, "jean@example.com")).matches("\\d{6}");
        assertThat(lastCode(CodeDelivery.WHATSAPP, "+24177123456")).matches("\\d{6}");
        assertThat(jdbc.queryForObject("select phone_e164 from customer where email = ?", String.class,
                "jean@example.com")).isEqualTo("+24177123456");
        assertThat(jdbc.queryForObject("select phone_verified_at from customer where email = ?",
                java.sql.Timestamp.class, "jean@example.com")).isNull();
    }

    @Test
    void passwordResetIsNotACodeChannelOfTheVerificationEndpoints() throws Exception {
        postJson("/v1/auth/verify", """
                {"channel":"PASSWORD_RESET","target":"jean@example.com","code":"123456"}
                """).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("CHANNEL_NOT_SUPPORTED"));
        postJson("/v1/auth/verify/resend", """
                {"channel":"PASSWORD_RESET","target":"jean@example.com"}
                """).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("CHANNEL_NOT_SUPPORTED"));

        verify(sender, never()).send(any(), anyString(), anyString());
    }

    @Test
    void duplicateEmailsAreRejected() throws Exception {
        registerAndVerifyEmail("dup@example.com");
        postJson("/v1/auth/register", """
                {"firstName":"B","lastName":"B","email":"DUP@example.com","phone":"077555000","password":"Secret123!"}
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

    @Test
    void signingInOnASecondDeviceSignsTheFirstOneOut() throws Exception {
        String email = "twodevices@example.com";
        registerAndVerifyEmail(email);
        String login = "{\"identifier\":\"%s\",\"password\":\"Secret123!\"}".formatted(email);

        String first = postJson("/v1/auth/login", login).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String firstAccess = JsonPath.read(first, "$.accessToken");
        String firstRefresh = JsonPath.read(first, "$.refreshToken");
        mvc.perform(get("/v1/me").header("Authorization", "Bearer " + firstAccess)).andExpect(status().isOk());

        String second = postJson("/v1/auth/login", login).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String secondAccess = JsonPath.read(second, "$.accessToken");

        mvc.perform(get("/v1/me").header("Authorization", "Bearer " + firstAccess))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("SESSION_REPLACED"));
        postJson("/v1/auth/refresh", "{\"refreshToken\":\"%s\"}".formatted(firstRefresh))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/v1/me").header("Authorization", "Bearer " + secondAccess)).andExpect(status().isOk());
    }

    @Test
    void theLockoutLastsThirtyMinutes() throws Exception {
        String email = "thirty@example.com";
        registerAndVerifyEmail(email);
        for (int i = 0; i < 5; i++) {
            postJson("/v1/auth/login", "{\"identifier\":\"%s\",\"password\":\"wrong-password\"}"
                    .formatted(email)).andExpect(status().isUnauthorized());
        }
        Long minutes = jdbc.queryForObject(
                "select round(extract(epoch from (locked_until - now())) / 60) from customer where email = ?",
                Long.class, email);
        assertThat(minutes).isBetween(29L, 30L);
    }
}
