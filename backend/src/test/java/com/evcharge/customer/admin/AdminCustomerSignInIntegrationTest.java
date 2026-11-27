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

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminCustomerSignInIntegrationTest extends PostgresTestBase {

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

    private AdminTestData data;
    private String root;

    @BeforeEach
    void prepare() {
        data = new AdminTestData(jdbc);
        root = new AdminFixtures(admins, passwords, tokens).create(AdminRole.SUPER_ADMIN, Set.of()).accessToken();
        TenantContext.clear();
    }

    private ResultActions detail(UUID customer) throws Exception {
        return mvc.perform(get("/v1/admin/customers/GA/" + customer).header("Authorization", "Bearer " + root))
                .andExpect(status().isOk());
    }

    private void identity(UUID customer, String provider) {
        TenantContext.runAs("GA", () -> jdbc.update("insert into customer_identity (id, customer_id, provider, "
                + "subject, created_at) values (?, ?, ?, ?, now())", UUID.randomUUID(), customer, provider,
                UUID.randomUUID().toString()));
    }

    private void passwordless(UUID customer) {
        TenantContext.runAs("GA", () -> jdbc.update("update customer set password_login = false where id = ?",
                customer));
    }

    @Test
    void aCustomerWhoRegisteredWithAPasswordShowsOnlyThePassword() throws Exception {
        detail(data.customer("GA", "ACTIVE"))
                .andExpect(jsonPath("$.signInMethods", contains("PASSWORD")));
    }

    @Test
    void anAppleOnlyCustomerShowsOnlyApple() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE");
        identity(customer, "APPLE");
        passwordless(customer);

        detail(customer).andExpect(jsonPath("$.signInMethods", contains("APPLE")));
    }

    @Test
    void everyMethodTheCustomerCanUseIsListed() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE");
        identity(customer, "GOOGLE");
        identity(customer, "APPLE");

        detail(customer).andExpect(jsonPath("$.signInMethods", contains("PASSWORD", "APPLE", "GOOGLE")));
    }
}
