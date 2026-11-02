package com.evcharge.customer.admin;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.application.port.AdminMailer;
import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.application.port.AdminTokenIssuer;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminPasswordResetIntegrationTest extends PostgresTestBase {

    private static final String NEW_PASSWORD = "Brand-New-Pass-42";

    @Autowired
    MockMvc mvc;

    @Autowired
    AdminRepository admins;

    @Autowired
    AdminPasswordHasher passwords;

    @Autowired
    AdminTokenIssuer tokens;

    @MockitoBean
    AdminMailer mailer;

    private AdminFixtures fixtures;

    @BeforeEach
    void prepare() {
        fixtures = new AdminFixtures(admins, passwords, tokens);
        when(mailer.sendPasswordReset(any(), any(), any())).thenReturn(true);
        TenantContext.clear();
    }

    private ResultActions post(String path, String json) throws Exception {
        return mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(path)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private ResultActions login(String email, String password) throws Exception {
        return post("/v1/admin/auth/login", "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password));
    }

    private String requestLink(String email) throws Exception {
        post("/v1/admin/auth/password/forgot", "{\"email\":\"%s\"}".formatted(email))
                .andExpect(status().isNoContent());
        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
        verify(mailer).sendPasswordReset(eq(email), link.capture(), any());
        return link.getValue().substring(link.getValue().indexOf("token=") + 6);
    }

    @Test
    void aResetLinkSetsANewPasswordOnceAndClearsTheLockout() throws Exception {
        AdminFixtures.Created admin = fixtures.create(AdminRole.SUPER_ADMIN, Set.of());
        for (int attempt = 0; attempt < 5; attempt++) {
            login(admin.email(), "wrong-password").andExpect(status().isUnauthorized());
        }
        login(admin.email(), AdminFixtures.PASSWORD).andExpect(status().isTooManyRequests());
        String token = requestLink(admin.email());

        post("/v1/admin/auth/password/reset", "{\"token\":\"%s\",\"password\":\"%s\"}".formatted(token, NEW_PASSWORD))
                .andExpect(status().isNoContent());

        login(admin.email(), NEW_PASSWORD).andExpect(status().isOk());
        login(admin.email(), AdminFixtures.PASSWORD).andExpect(status().isUnauthorized());
        post("/v1/admin/auth/password/reset", "{\"token\":\"%s\",\"password\":\"%s\"}".formatted(token, NEW_PASSWORD))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("RESET_LINK_INVALID"));
    }

    @Test
    void anUnknownEmailGetsTheSameAnswerAndNoMail() throws Exception {
        post("/v1/admin/auth/password/forgot", "{\"email\":\"nobody@example.com\"}")
                .andExpect(status().isNoContent());

        verify(mailer, never()).sendPasswordReset(any(), any(), any());
    }

    @Test
    void aWeakPasswordIsRefusedAndTheLinkStaysUsable() throws Exception {
        AdminFixtures.Created admin = fixtures.create(AdminRole.SUPER_ADMIN, Set.of());
        String token = requestLink(admin.email());

        post("/v1/admin/auth/password/reset", "{\"token\":\"%s\",\"password\":\"short1\"}".formatted(token))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("WEAK_PASSWORD"));
        post("/v1/admin/auth/password/reset", "{\"token\":\"%s\",\"password\":\"%s\"}".formatted(token, NEW_PASSWORD))
                .andExpect(status().isNoContent());
    }

    @Test
    void anInvalidTokenIsRefused() throws Exception {
        post("/v1/admin/auth/password/reset", "{\"token\":\"nope\",\"password\":\"%s\"}".formatted(NEW_PASSWORD))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("RESET_LINK_INVALID"));
    }
}
