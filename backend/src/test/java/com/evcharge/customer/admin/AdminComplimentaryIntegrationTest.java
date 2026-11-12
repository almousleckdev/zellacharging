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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminComplimentaryIntegrationTest extends PostgresTestBase {

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
    private String root;

    @BeforeEach
    void prepare() {
        fixtures = new AdminFixtures(admins, passwords, tokens);
        data = new AdminTestData(jdbc);
        root = fixtures.create(AdminRole.SUPER_ADMIN, Set.of()).accessToken();
        TenantContext.clear();
    }

    private ResultActions call(MockHttpServletRequestBuilder request, String token) throws Exception {
        return mvc.perform(request.header("Authorization", "Bearer " + token));
    }

    private static MockHttpServletRequestBuilder grant(UUID customer, String body) {
        return put("/v1/admin/customers/GA/" + customer + "/complimentary").contentType("application/json")
                .content(body);
    }

    private int grants(UUID customer) {
        return TenantContext.callAs("GA", () -> jdbc.queryForObject(
                "select count(*) from customer_complimentary where customer_id = ?", Integer.class, customer));
    }

    @Test
    void aSuperAdminGrantsThenRevokesFreeChargingAndEachStepIsAudited() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE");

        call(grant(customer, "{\"reason\":\"CEO\"}"), root).andExpect(status().isNoContent());
        assertThat(grants(customer)).isEqualTo(1);
        call(get("/v1/admin/customers/GA/" + customer), root).andExpect(status().isOk())
                .andExpect(jsonPath("$.freeAccess.reason").value("CEO"));
        call(get("/v1/admin/complimentary?country=GA"), root).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(customer.toString()));

        call(delete("/v1/admin/customers/GA/" + customer + "/complimentary"), root)
                .andExpect(status().isNoContent());
        assertThat(grants(customer)).isZero();
        call(get("/v1/admin/customers/GA/" + customer), root).andExpect(jsonPath("$.freeAccess").doesNotExist());
        call(get("/v1/admin/audit?country=GA&q=" + customer), root).andExpect(jsonPath("$.total").value(2));
    }

    @Test
    void anAdminWithoutThePermissionIsRefusedEvenWithAllCustomerPermissions() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE");
        String admin = fixtures.create(AdminRole.ADMIN, Set.of("GA")).accessToken();

        call(grant(customer, "{\"reason\":\"DG\"}"), admin).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PERMISSION_DENIED"));
        call(get("/v1/admin/complimentary?country=GA"), admin).andExpect(status().isForbidden());
        assertThat(grants(customer)).isZero();
    }

    @Test
    void aSuperAdminCanDelegateThePermissionToAnotherAdmin() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE");
        var permissions = EnumSet.copyOf(AdminRole.ADMIN.permissions());
        permissions.add(Permission.COMPLIMENTARY_MANAGE);
        String delegate = fixtures.createWith(AdminRole.ADMIN, Set.of("GA"), permissions).accessToken();

        call(grant(customer, "{\"reason\":\"DG\"}"), delegate).andExpect(status().isNoContent());
        assertThat(grants(customer)).isEqualTo(1);
    }

    @Test
    void aReasonIsRequiredAndAnEndDateMustBeInTheFuture() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE");
        Instant past = Instant.now().minus(1, ChronoUnit.DAYS);

        call(grant(customer, "{\"reason\":\"  \"}"), root).andExpect(status().isBadRequest());
        call(grant(customer, "{\"reason\":\"CEO\",\"expiresAt\":\"" + past + "\"}"), root)
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_COMPLIMENTARY"));
        call(grant(UUID.randomUUID(), "{\"reason\":\"CEO\"}"), root).andExpect(status().isNotFound());
        assertThat(grants(customer)).isZero();
    }
}
