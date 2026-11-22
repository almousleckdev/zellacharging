package com.evcharge.customer.admin;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminTokenIssuer;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminPartnerKeyIntegrationTest extends PostgresTestBase {

    private static final String KEYS = "/v1/admin/partners/NDZELA/keys";

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    AdminRepository admins;

    @Autowired
    AdminPasswordHasher passwords;

    @Autowired
    AdminTokenIssuer tokens;

    @Autowired
    ObjectMapper mapper;

    private AdminFixtures fixtures;
    private String root;

    @BeforeEach
    void prepare() {
        fixtures = new AdminFixtures(admins, passwords, tokens);
        root = fixtures.create(AdminRole.SUPER_ADMIN, Set.of()).accessToken();
        TenantContext.clear();
    }

    private ResultActions as(String token, MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.header("Authorization", "Bearer " + token));
    }

    private JsonNode issue() throws Exception {
        String body = as(root, post(KEYS).contentType("application/json").content("{\"country\":\"GA\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(body);
    }

    @Test
    void superAdminIssuesAKeyThatWorksAndIsShownOnce() throws Exception {
        JsonNode issued = issue();
        String key = issued.get("key").asText();

        assertThat(key).startsWith("zpk_");
        mvc.perform(get("/v1/partners/ping").header("X-Api-Key", key)).andExpect(status().isOk());
        String listed = as(root, get(KEYS)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(listed).contains(issued.get("id").asText()).doesNotContain(key).doesNotContain("keyHash");
    }

    @Test
    void revokeStopsTheKeyAtOnce() throws Exception {
        JsonNode issued = issue();

        as(root, delete(KEYS + "/" + issued.get("id").asText())).andExpect(status().isNoContent());

        mvc.perform(get("/v1/partners/ping").header("X-Api-Key", issued.get("key").asText()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminWithoutAdminsManageIsForbidden() throws Exception {
        String support = fixtures.create(AdminRole.SUPPORT, Set.of("GA")).accessToken();

        as(support, post(KEYS).contentType("application/json").content("{\"country\":\"GA\"}"))
                .andExpect(status().isForbidden());
        as(support, get(KEYS)).andExpect(status().isForbidden());
    }

    @Test
    void unknownPartnerCountryOrKeyIs404() throws Exception {
        as(root, post("/v1/admin/partners/ACME/keys").contentType("application/json").content("{\"country\":\"GA\"}"))
                .andExpect(status().isNotFound());
        as(root, post(KEYS).contentType("application/json").content("{\"country\":\"ZZ\"}"))
                .andExpect(status().isNotFound());
        as(root, delete(KEYS + "/" + java.util.UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test
    void issuingAndRevokingWriteAuditRowsWithoutTheKey() throws Exception {
        JsonNode issued = issue();
        as(root, delete(KEYS + "/" + issued.get("id").asText())).andExpect(status().isNoContent());

        List<String> rows = TenantContext.callAs("GA", () -> jdbc.queryForList("select action || ' ' || "
                + "coalesce(subject, '') || ' ' || coalesce(detail, '') from audit_record where subject = ?",
                String.class, issued.get("id").asText()));

        assertThat(rows).hasSize(2);
        assertThat(rows).anyMatch(row -> row.startsWith("PARTNER_KEY_ISSUED"));
        assertThat(rows).anyMatch(row -> row.startsWith("PARTNER_KEY_REVOKED"));
        assertThat(String.join(" ", rows)).doesNotContain(issued.get("key").asText());
    }
}
