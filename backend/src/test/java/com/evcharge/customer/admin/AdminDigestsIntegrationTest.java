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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminDigestsIntegrationTest extends PostgresTestBase {

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
    private String marker;

    @BeforeEach
    void prepare() {
        data = new AdminTestData(jdbc);
        root = new AdminFixtures(admins, passwords, tokens).create(AdminRole.SUPER_ADMIN, Set.of()).accessToken();
        marker = "Zq" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        TenantContext.clear();
    }

    private ResultActions call(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.header("Authorization", "Bearer " + root));
    }

    @Test
    void theSessionsPageListsEachCustomerOnceWithTheLastChargeAndTheTotals() throws Exception {
        UUID frequent = data.customer("GA", "ACTIVE", marker + "Many", "Charges", null, Instant.now());
        UUID once = data.customer("GA", "ACTIVE", marker + "Once", "Charge", null, Instant.now());
        for (int connector = 61; connector < 66; connector++) {
            data.session("GA", frequent, connector, "COMPLETED", "2.0", 500L);
        }
        data.session("GA", once, 70, "COMPLETED", "1.0", 200L);

        call(get("/v1/admin/sessions/by-customer?country=GA&q=" + marker + "&sort=count:desc")).andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.items[0].customerId").value(frequent.toString()))
                .andExpect(jsonPath("$.items[0].sessions").value(5))
                .andExpect(jsonPath("$.items[0].spentMinor").value(2500))
                .andExpect(jsonPath("$.items[0].last.state").value("COMPLETED"))
                .andExpect(jsonPath("$.items[1].sessions").value(1));
    }

    @Test
    void thePaymentsPageListsEachCustomerOnceWithTheLastOperation() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE", marker + "Pay", "Many", null, Instant.now());
        data.wallet("GA", customer, "XAF", 0, 0);
        data.topup("GA", customer, "SUCCEEDED", 1000);
        data.topup("GA", customer, "SUCCEEDED", 2000);
        data.topup("GA", customer, "FAILED", 500);

        call(get("/v1/admin/payments/by-customer?country=GA&q=" + marker)).andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1)).andExpect(jsonPath("$.items[0].operations").value(3))
                .andExpect(jsonPath("$.items[0].topupsMinor").value(3000))
                .andExpect(jsonPath("$.items[0].last.customerId").value(customer.toString()));
    }

    @Test
    void theCommissionsPageListsEachCustomerOnce() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE", marker + "Fee", "Many", null, Instant.now());
        data.session("GA", customer, 81, "COMPLETED", "1.0", 400L);
        data.session("GA", customer, 82, "COMPLETED", "1.0", 600L);

        call(get("/v1/admin/commissions?country=GA&q=" + marker)).andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1)).andExpect(jsonPath("$.items[0].sessions").value(2))
                .andExpect(jsonPath("$.items[0].electricMinor").value(1000));
    }
}
