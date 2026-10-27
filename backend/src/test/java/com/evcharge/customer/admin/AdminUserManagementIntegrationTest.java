package com.evcharge.customer.admin;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.application.port.AdminMailer;
import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.application.port.AdminTokenIssuer;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.shared.TenantContext;
import com.jayway.jsonpath.JsonPath;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminUserManagementIntegrationTest extends PostgresTestBase {

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
    private AdminFixtures.Created root;

    @BeforeEach
    void prepare() {
        fixtures = new AdminFixtures(admins, passwords, tokens);
        root = fixtures.create(AdminRole.SUPER_ADMIN, Set.of());
        when(mailer.sendInvitation(any(), any(), any())).thenReturn(true);
        TenantContext.clear();
    }

    private ResultActions send(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
                               String token, String body) throws Exception {
        var builder = request.header("Authorization", "Bearer " + token);
        return mvc.perform(body == null ? builder : builder.contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private String invitation(String email, String role, String countries) {
        return """
                {"email":"%s","role":"%s","countries":%s}""".formatted(email, role, countries);
    }

    private String invite(String email, String role, String countries) throws Exception {
        String response = send(post("/v1/admin/users"), root.accessToken(), invitation(email, role, countries))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.admin.id");
    }

    private String freshEmail() {
        return UUID.randomUUID() + "@zella.test";
    }

    @Test
    void invitingCreatesAPendingUserWithTheRolePresetAndTheChosenCountries() throws Exception {
        String email = freshEmail();

        send(post("/v1/admin/users"), root.accessToken(), invitation(email, "SUPPORT", "[\"GA\"]"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.emailSent").value(true))
                .andExpect(jsonPath("$.admin.status").value("INVITED"))
                .andExpect(jsonPath("$.admin.role").value("SUPPORT"))
                .andExpect(jsonPath("$.admin.countries[0]").value("GA"))
                .andExpect(jsonPath("$.admin.permissions[?(@=='CUSTOMERS_READ')]").exists())
                .andExpect(jsonPath("$.admin.permissions[?(@=='PAYMENTS_READ')]").doesNotExist());

        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
        verify(mailer, atLeastOnce()).sendInvitation(eq(email), link.capture(), any());
        assertThat(link.getValue()).startsWith("http://localhost:3000/accept-invite?token=");
    }

    @Test
    void customPermissionsOverrideTheRolePreset() throws Exception {
        send(post("/v1/admin/users"), root.accessToken(), """
                {"email":"%s","role":"FINANCE","permissions":["OVERVIEW_READ","AUDIT_READ"],"countries":["ML"]}"""
                .formatted(freshEmail())).andExpect(status().isCreated())
                .andExpect(jsonPath("$.admin.permissions.length()").value(2))
                .andExpect(jsonPath("$.admin.permissions[?(@=='AUDIT_READ')]").exists());
    }

    @Test
    void reportsWhenTheInvitationEmailCouldNotBeSent() throws Exception {
        when(mailer.sendInvitation(any(), any(), any())).thenReturn(false);

        send(post("/v1/admin/users"), root.accessToken(), invitation(freshEmail(), "ADMIN", "[\"GA\"]"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.emailSent").value(false));
    }

    @Test
    void theDefinitionIsValidatedOnTheServer() throws Exception {
        String email = freshEmail();
        invite(email, "ADMIN", "[\"GA\"]");

        send(post("/v1/admin/users"), root.accessToken(), invitation(email, "ADMIN", "[\"GA\"]"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("EMAIL_ALREADY_USED"));
        send(post("/v1/admin/users"), root.accessToken(), invitation(freshEmail(), "SUPER_ADMIN", "[\"GA\"]"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_ROLE"));
        send(post("/v1/admin/users"), root.accessToken(), """
                {"email":"%s","role":"ADMIN","permissions":["ADMINS_MANAGE"],"countries":["GA"]}"""
                .formatted(freshEmail())).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PERMISSIONS"));
        send(post("/v1/admin/users"), root.accessToken(), invitation(freshEmail(), "ADMIN", "[]"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_COUNTRIES"));
        send(post("/v1/admin/users"), root.accessToken(), invitation(freshEmail(), "ADMIN", "[\"FR\"]"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_COUNTRIES"));
    }

    @Test
    void onlyAnAdministratorWhoMayManageUsersCanUseTheseEndpoints() throws Exception {
        AdminFixtures.Created gabon = fixtures.create(AdminRole.ADMIN, Set.of("GA"));

        send(get("/v1/admin/users"), gabon.accessToken(), null).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PERMISSION_DENIED"));
        send(post("/v1/admin/users"), gabon.accessToken(), invitation(freshEmail(), "ADMIN", "[\"GA\"]"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/v1/admin/users")).andExpect(status().isUnauthorized());
    }

    @Test
    void theListSupportsSearchAndStatusFilters() throws Exception {
        String email = "Findable-" + UUID.randomUUID().toString().substring(0, 8) + "@zella.test";
        invite(email, "OPERATIONS", "[\"ML\"]");

        send(get("/v1/admin/users?q=" + email.substring(0, 18)), root.accessToken(), null).andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1)).andExpect(jsonPath("$.items[0].email")
                        .value(email.toLowerCase()));
        send(get("/v1/admin/users?q=" + email.substring(0, 18) + "&status=ACTIVE"), root.accessToken(), null)
                .andExpect(jsonPath("$.total").value(0));
        send(get("/v1/admin/users?status=NOPE"), root.accessToken(), null).andExpect(status().isBadRequest());
    }

    @Test
    void updatingAccessChangesTheRolePermissionsAndCountries() throws Exception {
        String id = invite(freshEmail(), "SUPPORT", "[\"GA\"]");

        send(put("/v1/admin/users/" + id), root.accessToken(), """
                {"role":"FINANCE","permissions":["PAYMENTS_READ"],"countries":["GA","ML"]}""")
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("FINANCE"))
                .andExpect(jsonPath("$.permissions.length()").value(1))
                .andExpect(jsonPath("$.countries.length()").value(2));
    }

    @Test
    void theSuperAdminAndTheCallerThemselvesAreProtected() throws Exception {
        send(post("/v1/admin/users/" + root.id() + "/disable"), root.accessToken(), null)
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("ADMIN_PROTECTED"));
        send(put("/v1/admin/users/" + root.id()), root.accessToken(), """
                {"role":"ADMIN","countries":["GA"]}""").andExpect(status().isConflict());
        send(delete("/v1/admin/users/" + root.id()), root.accessToken(), null).andExpect(status().isConflict());
    }

    @Test
    void disablingBlocksSignInAndEnablingRestoresIt() throws Exception {
        AdminFixtures.Created user = fixtures.create(AdminRole.ADMIN, Set.of("GA"));
        String login = """
                {"email":"%s","password":"%s"}""".formatted(user.email(), AdminFixtures.PASSWORD);

        send(post("/v1/admin/users/" + user.id() + "/disable"), root.accessToken(), null).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISABLED"));
        mvc.perform(post("/v1/admin/auth/login").contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isUnauthorized());
        send(post("/v1/admin/users/" + user.id() + "/enable"), root.accessToken(), null).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        mvc.perform(post("/v1/admin/auth/login").contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isOk());
    }

    @Test
    void onlyAPendingInvitationCanBeDeletedAndResendingIssuesANewLink() throws Exception {
        String pending = invite(freshEmail(), "ADMIN", "[\"GA\"]");
        send(post("/v1/admin/users/" + pending + "/invitation"), root.accessToken(), null).andExpect(status().isOk())
                .andExpect(jsonPath("$.emailSent").value(true));
        send(delete("/v1/admin/users/" + pending), root.accessToken(), null).andExpect(status().isNoContent());
        send(get("/v1/admin/users/" + pending), root.accessToken(), null).andExpect(status().isNotFound());

        AdminFixtures.Created active = fixtures.create(AdminRole.ADMIN, Set.of("GA"));
        send(delete("/v1/admin/users/" + active.id()), root.accessToken(), null).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ADMIN_NOT_DELETABLE"));
        send(post("/v1/admin/users/" + active.id() + "/invitation"), root.accessToken(), null)
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("ADMIN_NOT_PENDING"));
    }
}
