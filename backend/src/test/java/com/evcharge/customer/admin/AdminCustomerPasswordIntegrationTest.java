package com.evcharge.customer.admin;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.application.port.AdminTokenIssuer;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminCustomerPasswordIntegrationTest extends PostgresTestBase {

    private static final String NEW_PASSWORD = "Nouveau-Pass-2026";

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
    private AdminTestData data;
    private UUID customer;

    @BeforeEach
    void prepare() {
        fixtures = new AdminFixtures(admins, passwords, tokens);
        data = new AdminTestData(jdbc);
        customer = data.customer("GA", "ACTIVE");
        TenantContext.runAs("GA", () -> jdbc.update("update customer set password_login = false, "
                + "failed_login_attempts = 5, locked_until = now() + interval '30 minutes' where id = ?", customer));
        TenantContext.runAs("GA", () -> jdbc.update("insert into refresh_token (id, customer_id, family_id, "
                + "token_hash, expires_at) values (?, ?, ?, ?, now() + interval '1 day')", UUID.randomUUID(),
                customer, UUID.randomUUID(), UUID.randomUUID().toString()));
        TenantContext.clear();
    }

    private ResultActions change(String token, String password) throws Exception {
        return mvc.perform(put("/v1/admin/customers/GA/" + customer + "/password")
                .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"" + password + "\"}"));
    }

    private String token(AdminRole role) {
        return fixtures.create(role, Set.of("GA")).accessToken();
    }

    private long count(String sql) {
        return TenantContext.callAs("GA", () -> jdbc.queryForObject(sql, Long.class, customer));
    }

    @Test
    void aSuperAdminSetsAPasswordTheCustomerCanSignInWith() throws Exception {
        change(token(AdminRole.SUPER_ADMIN), NEW_PASSWORD).andExpect(status().isNoContent());

        mvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"" + customer + "@example.com\",\"password\":\"" + NEW_PASSWORD
                                + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void anAdminChangeSignsTheCustomerOutAndIsAuditedWithoutThePassword() throws Exception {
        change(token(AdminRole.ADMIN), NEW_PASSWORD).andExpect(status().isNoContent());

        assertThat(count("select count(*) from refresh_token where customer_id = ? and revoked_at is null"))
                .isZero();
        assertThat(count("select count(*) from audit_record where subject is not null and actor_id = ? "
                + "and action = 'PASSWORD_SET_BY_SUPPORT'")).isOne();
        assertThat(jdbc.queryForObject("select count(*) from admin_audit_record where action = "
                + "'CUSTOMER_PASSWORD_CHANGED' and subject = ? and detail is null", Long.class, customer.toString()))
                .isOne();
    }

    @Test
    void otherRolesAreRefusedEvenWithTheRightToManageCustomers() throws Exception {
        String support = fixtures.createWith(AdminRole.SUPPORT, Set.of("GA"),
                Set.of(Permission.CUSTOMERS_READ, Permission.CUSTOMERS_MANAGE)).accessToken();

        change(support, NEW_PASSWORD).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PERMISSION_DENIED"));
        assertThat(count("select count(*) from customer where id = ? and password_login")).isZero();
    }

    @Test
    void anAdminOfAnotherCountryIsRefused() throws Exception {
        change(fixtures.create(AdminRole.ADMIN, Set.of("ML")).accessToken(), NEW_PASSWORD)
                .andExpect(status().isForbidden());
    }

    @Test
    void aPasswordThatIsTooShortIsRefused() throws Exception {
        change(token(AdminRole.SUPER_ADMIN), "court").andExpect(status().isBadRequest());
    }
}
