package com.evcharge.customer.admin.infrastructure;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.application.port.SecretCipher;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.admin.domain.AdminStatus;
import com.evcharge.customer.admin.domain.AdminUser;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminAuthIntegrationTest extends PostgresTestBase {

    private static final String PASSWORD = "Str0ng-Pass!";

    @Autowired
    MockMvc mvc;

    @Autowired
    Rfc6238Totp totp;

    @Autowired
    AdminRepository admins;

    @Autowired
    AdminPasswordHasher passwords;

    @Autowired
    SecretCipher cipher;

    private long baseStep;

    private record Enrolled(String email, String secret) {
    }

    private Enrolled enrolledAdmin() {
        String secret = totp.newSecret();
        return new Enrolled(createAdmin(cipher.encrypt(secret)), secret);
    }

    private String createAdmin(String encryptedTotpSecret) {
        String email = UUID.randomUUID() + "@zella.test";
        AdminUser user = AdminUser.create(UUID.randomUUID(), email, passwords.hash(PASSWORD), AdminRole.SUPER_ADMIN,
                AdminStatus.ACTIVE, Set.of(), Set.of()).withName("Test", "Admin");
        admins.create(encryptedTotpSecret == null ? user : user.withTotpSecret(encryptedTotpSecret), Instant.now());
        return email;
    }

    private ResultActions postJson(String path, String json) throws Exception {
        return mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @BeforeEach
    void pinTimeStep() {
        baseStep = Instant.now().getEpochSecond() / 30;
        TestSecurityContextHolder.clearContext();
    }

    @AfterEach
    void clearSecurity() {
        TestSecurityContextHolder.clearContext();
    }

    private String codeAtOffset(String secret, long stepOffset) {
        return totp.codeAt(Base32.decode(secret), baseStep + stepOffset);
    }

    private ResultActions login(String email, String password) throws Exception {
        return postJson("/v1/admin/auth/login", """
                {"email":"%s","password":"%s"}""".formatted(email, password));
    }

    private ResultActions complete(String challenge, String code) throws Exception {
        return postJson("/v1/admin/auth/totp", """
                {"challengeToken":"%s","code":"%s"}""".formatted(challenge, code));
    }

    private String field(ResultActions result, String name) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$." + name);
    }

    private String signIn(Enrolled admin) throws Exception {
        ResultActions step = login(admin.email(), PASSWORD).andExpect(status().isOk());
        return field(complete(field(step, "challengeToken"), codeAtOffset(admin.secret(), 0))
                .andExpect(status().isOk()), "accessToken");
    }

    @Test
    void firstSignInEnrollsTotpThenEveryLaterSignInDemandsAFreshCode() throws Exception {
        String email = createAdmin(null);
        ResultActions first = login(email, PASSWORD).andExpect(status().isOk())
                .andExpect(jsonPath("$.stage").value("TOTP_ENROLLMENT"));
        String secret = field(first, "secret");
        String access = field(complete(field(first, "challengeToken"), codeAtOffset(secret, 0))
                .andExpect(status().isOk()), "accessToken");
        mvc.perform(get("/v1/admin/me").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("SUPER_ADMIN"))
                .andExpect(jsonPath("$.countries.length()").value(2));

        String challenge = field(login(email, PASSWORD).andExpect(jsonPath("$.stage").value("TOTP_REQUIRED")),
                "challengeToken");
        complete(challenge, codeAtOffset(secret, 0)).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOTP_CODE"));
        complete(challenge, codeAtOffset(secret, 1)).andExpect(status().isOk());
    }

    @Test
    void aWrongPasswordIsRejectedWithoutRevealingWhichPartFailed() throws Exception {
        login(enrolledAdmin().email(), "not-the-password").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        login("nobody@zella.test", PASSWORD).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void aWrongCodeOrForgedChallengeNeverIssuesTokens() throws Exception {
        Enrolled admin = enrolledAdmin();
        String challenge = field(login(admin.email(), PASSWORD).andExpect(status().isOk()), "challengeToken");
        complete(challenge, "000000").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOTP_CODE"));
        complete("not-a-token", codeAtOffset(admin.secret(), 0)).andExpect(status().isUnauthorized());
    }

    @Test
    void repeatedWrongCodesLockTheAccount() throws Exception {
        Enrolled admin = enrolledAdmin();
        String challenge = field(login(admin.email(), PASSWORD).andExpect(status().isOk()), "challengeToken");
        for (int attempt = 0; attempt < 5; attempt++) {
            complete(challenge, "000000").andExpect(status().isUnauthorized());
        }
        complete(challenge, codeAtOffset(admin.secret(), 0)).andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("ACCOUNT_LOCKED"));
    }

    @Test
    void customerAndAdminTokensAreNotInterchangeable() throws Exception {
        mvc.perform(get("/v1/admin/me").with(TestTenants.anyCustomer())).andExpect(status().isUnauthorized());
        TestSecurityContextHolder.clearContext();
        mvc.perform(get("/v1/admin/me")).andExpect(status().isUnauthorized());
        String access = signIn(enrolledAdmin());
        mvc.perform(get("/v1/wallet").header("Authorization", "Bearer " + access))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aChallengeTokenIsNotAnAccessToken() throws Exception {
        ResultActions step = login(enrolledAdmin().email(), PASSWORD).andExpect(status().isOk());
        mvc.perform(get("/v1/admin/me").header("Authorization", "Bearer " + field(step, "challengeToken")))
                .andExpect(status().isUnauthorized());
    }
}
