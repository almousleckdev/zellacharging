package com.evcharge.customer.admin;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminRefreshTokenRepository;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.application.port.AdminTokenIssuer;
import com.evcharge.customer.admin.domain.AdminRefreshToken;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.shared.Sha256;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminPasswordIntegrationTest extends PostgresTestBase {

    private static final String NEW_PASSWORD = "Brand-New-Pass-42";

    @Autowired
    MockMvc mvc;

    @Autowired
    AdminRepository admins;

    @Autowired
    AdminPasswordHasher passwords;

    @Autowired
    AdminTokenIssuer tokens;

    @Autowired
    AdminRefreshTokenRepository refreshTokens;

    private AdminFixtures fixtures;

    @BeforeEach
    void prepare() {
        fixtures = new AdminFixtures(admins, passwords, tokens);
        TenantContext.clear();
    }

    private ResultActions change(AdminFixtures.Created admin, String current, String next) throws Exception {
        return mvc.perform(post("/v1/admin/me/password").header("Authorization", "Bearer " + admin.accessToken())
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"currentPassword":"%s","newPassword":"%s"}""".formatted(current, next)));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/v1/admin/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"%s","password":"%s"}""".formatted(email, password)));
    }

    @Test
    void changingThePasswordSwitchesTheCredentialAndEndsEverySession() throws Exception {
        AdminFixtures.Created admin = fixtures.create(AdminRole.SUPER_ADMIN, Set.of());
        UUID family = UUID.randomUUID();
        refreshTokens.save(new AdminRefreshToken(UUID.randomUUID(), admin.id(), family, Sha256.hex("raw-refresh"),
                Instant.now().plusSeconds(3600), null, null));

        change(admin, AdminFixtures.PASSWORD, NEW_PASSWORD).andExpect(status().isNoContent());

        login(admin.email(), NEW_PASSWORD).andExpect(status().isOk());
        login(admin.email(), AdminFixtures.PASSWORD).andExpect(status().isUnauthorized());
        assertThat(refreshTokens.findByHash(Sha256.hex("raw-refresh")).orElseThrow().revokedAt()).isNotNull();
    }

    @Test
    void aWrongCurrentPasswordIsRefusedAndNothingChanges() throws Exception {
        AdminFixtures.Created admin = fixtures.create(AdminRole.SUPER_ADMIN, Set.of());

        change(admin, "not-the-password", NEW_PASSWORD).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CURRENT_PASSWORD"));

        login(admin.email(), AdminFixtures.PASSWORD).andExpect(status().isOk());
    }

    @Test
    void aWeakOrUnchangedPasswordIsRefused() throws Exception {
        AdminFixtures.Created admin = fixtures.create(AdminRole.SUPER_ADMIN, Set.of());

        change(admin, AdminFixtures.PASSWORD, "short1").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WEAK_PASSWORD"));
        change(admin, AdminFixtures.PASSWORD, "onlyletterspassword").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WEAK_PASSWORD"));
        change(admin, AdminFixtures.PASSWORD, AdminFixtures.PASSWORD).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PASSWORD_UNCHANGED"));
    }

    @Test
    void guessingTheCurrentPasswordLocksTheAccount() throws Exception {
        AdminFixtures.Created admin = fixtures.create(AdminRole.SUPER_ADMIN, Set.of());

        for (int attempt = 0; attempt < 5; attempt++) {
            change(admin, "guess-" + attempt, NEW_PASSWORD).andExpect(status().isBadRequest());
        }

        change(admin, AdminFixtures.PASSWORD, NEW_PASSWORD).andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("ACCOUNT_LOCKED"));
    }

    @Test
    void aSessionIsRequired() throws Exception {
        mvc.perform(post("/v1/admin/me/password").contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"x\",\"newPassword\":\"y\"}")).andExpect(status().isUnauthorized());
    }
}
