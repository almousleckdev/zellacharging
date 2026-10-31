package com.evcharge.customer.admin;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.application.port.AdminTokenIssuer;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminCustomerAvatarIntegrationTest extends PostgresTestBase {

    private static final byte[] PICTURE = {1, 2, 3, 4};

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

    private AdminFixtures fixtures;
    private String root;
    private String marker;
    private UUID withPicture;
    private UUID withoutPicture;

    @BeforeEach
    void prepare() {
        fixtures = new AdminFixtures(admins, passwords, tokens);
        AdminTestData data = new AdminTestData(jdbc);
        marker = "Av" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        root = fixtures.create(AdminRole.SUPER_ADMIN, Set.of()).accessToken();
        withPicture = data.customer("GA", "ACTIVE", marker + "Aline", "Obame", null, Instant.now());
        withoutPicture = data.customer("GA", "ACTIVE", marker + "Brice", "Nze", null, Instant.now());
        TenantContext.runAs("GA", () -> jdbc.update("insert into customer_avatar (customer_id, content_type, data, "
                + "updated_at) values (?, 'image/png', ?, now())", withPicture, PICTURE));
        TenantContext.clear();
    }

    private ResultActions fetch(String token, String path) throws Exception {
        return mvc.perform(get(path).header("Authorization", "Bearer " + token));
    }

    @Test
    void theListTellsWhichCustomersHaveAPicture() throws Exception {
        fetch(root, "/v1/admin/customers?country=GA&q=" + marker + "aline").andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].avatarVersion").isNumber());
        fetch(root, "/v1/admin/customers?country=GA&q=" + marker + "brice").andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].avatarVersion").doesNotExist());
    }

    @Test
    void theDetailAndThePictureAreServedToAnAdminWhoCanReadCustomers() throws Exception {
        fetch(root, "/v1/admin/customers/GA/" + withPicture).andExpect(jsonPath("$.avatarVersion").isNumber());

        byte[] body = fetch(root, "/v1/admin/customers/GA/" + withPicture + "/avatar").andExpect(status().isOk())
                .andExpect(content().contentType("image/png")).andReturn().getResponse().getContentAsByteArray();
        assertThat(body).isEqualTo(PICTURE);
    }

    @Test
    void aCustomerWithoutAPictureAnswersNoContent() throws Exception {
        fetch(root, "/v1/admin/customers/GA/" + withoutPicture + "/avatar").andExpect(status().isNoContent());
    }

    @Test
    void anAdminWhoCannotReadCustomersIsRefused() throws Exception {
        String operations = fixtures.create(AdminRole.OPERATIONS, Set.of("GA")).accessToken();

        fetch(operations, "/v1/admin/customers/GA/" + withPicture + "/avatar").andExpect(status().isForbidden());
    }
}
