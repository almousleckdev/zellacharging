package com.evcharge.customer.admin.infrastructure;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.AdminFixtures;
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

import java.time.Instant;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminOnboardingIntegrationTest extends PostgresTestBase {

    private static final String PASSWORD = "Invited-Pass-42";
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    @Autowired
    MockMvc mvc;

    @Autowired
    Rfc6238Totp totp;

    @Autowired
    AdminRepository admins;

    @Autowired
    AdminPasswordHasher passwords;

    @Autowired
    AdminTokenIssuer tokens;

    @MockitoBean
    AdminMailer mailer;

    private AdminFixtures.Created root;
    private long baseStep;

    @BeforeEach
    void prepare() {
        root = new AdminFixtures(admins, passwords, tokens).create(AdminRole.SUPER_ADMIN, Set.of());
        when(mailer.sendInvitation(any(), any(), any())).thenReturn(true);
        baseStep = Instant.now().getEpochSecond() / 30;
        TenantContext.clear();
    }

    private String invitedToken(String email, String role, String countries) throws Exception {
        mvc.perform(post("/v1/admin/users").header("Authorization", "Bearer " + root.accessToken())
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"email":"%s","role":"%s","countries":%s}""".formatted(email, role, countries)))
                .andExpect(status().isCreated());
        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
        verify(mailer, atLeastOnce()).sendInvitation(eq(email), link.capture(), any());
        return link.getValue().substring(link.getValue().indexOf("token=") + 6);
    }

    private ResultActions accept(String token, String password) throws Exception {
        return mvc.perform(post("/v1/admin/auth/invitation").contentType(MediaType.APPLICATION_JSON).content("""
                {"token":"%s","password":"%s"}""".formatted(token, password)));
    }

    private String field(ResultActions result, String name) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$." + name);
    }

    private String onboard(String email, String role, String countries) throws Exception {
        ResultActions step = accept(invitedToken(email, role, countries), PASSWORD).andExpect(status().isOk())
                .andExpect(jsonPath("$.stage").value("TOTP_ENROLLMENT"));
        String code = totp.codeAt(Base32.decode(field(step, "secret")), baseStep);
        ResultActions tokensResponse = mvc.perform(post("/v1/admin/auth/totp").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"challengeToken":"%s","code":"%s"}""".formatted(field(step, "challengeToken"), code)))
                .andExpect(status().isOk());
        return field(tokensResponse, "accessToken");
    }

    private ResultActions as(String token, org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder r,
                             String body) throws Exception {
        var builder = r.header("Authorization", "Bearer " + token);
        return mvc.perform(body == null ? builder : builder.contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void aGuestFollowsTheLinkSetsAPasswordEnablesMfaThenMustCompleteTheProfile() throws Exception {
        String email = UUID.randomUUID() + "@zella.test";
        String token = invitedToken(email, "ADMIN", "[\"GA\"]");

        mvc.perform(get("/v1/admin/auth/invitation?token=" + token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
        accept(token, "short1").andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("WEAK_PASSWORD"));
        String access = onboard(UUID.randomUUID() + "@zella.test", "ADMIN", "[\"GA\"]");

        as(access, get("/v1/admin/me"), null).andExpect(status().isOk())
                .andExpect(jsonPath("$.profileComplete").value(false));
        as(access, get("/v1/admin/overview"), null).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PROFILE_INCOMPLETE"));
        as(access, get("/v1/admin/customers"), null).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PROFILE_INCOMPLETE"));

        as(access, put("/v1/admin/me/profile"), """
                {"firstName":"Aline","lastName":"Obame","phone":"+24177123456"}""").andExpect(status().isNoContent());

        as(access, get("/v1/admin/me"), null).andExpect(jsonPath("$.profileComplete").value(true))
                .andExpect(jsonPath("$.firstName").value("Aline")).andExpect(jsonPath("$.phone").value("+24177123456"));
        as(access, get("/v1/admin/overview"), null).andExpect(status().isOk())
                .andExpect(jsonPath("$.countries.length()").value(1)).andExpect(jsonPath("$.countries[0].code").value("GA"));
        as(access, get("/v1/admin/overview?country=ML"), null).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COUNTRY_FORBIDDEN"));
    }

    @Test
    void anInvitationLinkWorksOnlyOnce() throws Exception {
        String token = invitedToken(UUID.randomUUID() + "@zella.test", "SUPPORT", "[\"ML\"]");

        accept(token, PASSWORD).andExpect(status().isOk());

        accept(token, PASSWORD).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("INVITATION_INVALID"));
        mvc.perform(get("/v1/admin/auth/invitation?token=" + token)).andExpect(status().isNotFound());
        accept("not-a-real-token", PASSWORD).andExpect(status().isNotFound());
    }

    @Test
    void aResentInvitationReplacesTheOldLink() throws Exception {
        String email = UUID.randomUUID() + "@zella.test";
        String oldToken = invitedToken(email, "ADMIN", "[\"GA\"]");
        String id = JsonPath.read(mvc.perform(get("/v1/admin/users?q=" + email)
                .header("Authorization", "Bearer " + root.accessToken())).andReturn().getResponse()
                .getContentAsString(), "$.items[0].id");

        mvc.perform(post("/v1/admin/users/" + id + "/invitation").header("Authorization", "Bearer "
                + root.accessToken())).andExpect(status().isOk());

        accept(oldToken, PASSWORD).andExpect(status().isNotFound());
    }

    @Test
    void phoneNumbersMustBeValidForGabonOrMaliAndAreOptional() throws Exception {
        String access = onboard(UUID.randomUUID() + "@zella.test", "ADMIN", "[\"GA\",\"ML\"]");
        String base = "{\"firstName\":\"A\",\"lastName\":\"B\",\"phone\":\"%s\"}";

        as(access, put("/v1/admin/me/profile"), base.formatted("+22370123456")).andExpect(status().isNoContent());
        as(access, put("/v1/admin/me/profile"), base.formatted("")).andExpect(status().isNoContent());
        as(access, get("/v1/admin/me"), null).andExpect(jsonPath("$.phone").doesNotExist());
        as(access, put("/v1/admin/me/profile"), base.formatted("+33612345678")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PHONE"));
        as(access, put("/v1/admin/me/profile"), base.formatted("077123456")).andExpect(status().isBadRequest());
        as(access, put("/v1/admin/me/profile"), "{\"firstName\":\" \",\"lastName\":\"B\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void aProfilePictureCanBeUploadedServedAndRemoved() throws Exception {
        String access = onboard(UUID.randomUUID() + "@zella.test", "ADMIN", "[\"GA\"]");
        var upload = put("/v1/admin/me/avatar").header("Authorization", "Bearer " + access)
                .contentType(MediaType.IMAGE_PNG).content(PNG);

        mvc.perform(upload).andExpect(status().isNoContent());

        as(access, get("/v1/admin/me"), null).andExpect(jsonPath("$.avatarVersion").isNumber());
        mvc.perform(get("/v1/admin/me/avatar").header("Authorization", "Bearer " + access)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG)).andExpect(content().bytes(PNG));
        mvc.perform(delete("/v1/admin/me/avatar").header("Authorization", "Bearer " + access))
                .andExpect(status().isNoContent());
        as(access, get("/v1/admin/me"), null).andExpect(jsonPath("$.avatarVersion").doesNotExist());
    }

    @Test
    void onlyRealImagesWithinTheSizeLimitAreAccepted() throws Exception {
        String access = onboard(UUID.randomUUID() + "@zella.test", "ADMIN", "[\"GA\"]");
        String auth = "Bearer " + access;

        mvc.perform(put("/v1/admin/me/avatar").header("Authorization", auth).contentType(MediaType.IMAGE_JPEG)
                .content(PNG)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_AVATAR"));
        mvc.perform(put("/v1/admin/me/avatar").header("Authorization", auth).contentType(MediaType.IMAGE_PNG)
                .content("<html>not an image</html>".getBytes())).andExpect(status().isBadRequest());
        byte[] huge = new byte[1_100_000];
        System.arraycopy(PNG, 0, huge, 0, PNG.length);
        mvc.perform(put("/v1/admin/me/avatar").header("Authorization", auth).contentType(MediaType.IMAGE_PNG)
                .content(huge)).andExpect(status().isBadRequest());
    }

    @Test
    void aSuperAdminCanSeeAnotherUsersPictureButOthersCannot() throws Exception {
        String access = onboard(UUID.randomUUID() + "@zella.test", "ADMIN", "[\"GA\"]");
        mvc.perform(put("/v1/admin/me/avatar").header("Authorization", "Bearer " + access)
                .contentType(MediaType.IMAGE_PNG).content(PNG)).andExpect(status().isNoContent());
        String id = field(as(access, get("/v1/admin/me"), null), "id");

        mvc.perform(get("/v1/admin/users/" + id + "/avatar").header("Authorization", "Bearer " + root.accessToken()))
                .andExpect(status().isOk()).andExpect(content().bytes(PNG));
        mvc.perform(get("/v1/admin/users/" + id + "/avatar").header("Authorization", "Bearer " + access))
                .andExpect(status().isForbidden());
        assertThat(id).isNotBlank();
    }
}
