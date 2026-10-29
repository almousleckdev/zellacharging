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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminCustomerHistoryIntegrationTest extends PostgresTestBase {

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
    void theCustomerListCarriesTheWalletAndSpendingSummaryAndSortsByName() throws Exception {
        UUID zed = data.customer("GA", "ACTIVE", marker + "Zed", "Last", null, Instant.now());
        UUID abel = data.customer("GA", "ACTIVE", marker + "Abel", "First", null, Instant.now());
        data.wallet("GA", abel, "XAF", 700, 0);
        data.session("GA", abel, 41, "COMPLETED", "3.0", 900L);

        call(get("/v1/admin/customers?country=GA&q=" + marker + "&sort=name:asc")).andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(abel.toString()))
                .andExpect(jsonPath("$.items[0].balanceMinor").value(700))
                .andExpect(jsonPath("$.items[0].spentMinor").value(900))
                .andExpect(jsonPath("$.items[0].completedSessions").value(1))
                .andExpect(jsonPath("$.items[1].id").value(zed.toString()))
                .andExpect(jsonPath("$.items[1].balanceMinor").doesNotExist());
    }

    @Test
    void sessionsSortByAmountInEitherDirection() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE", marker + "Sort", "Test", null, Instant.now());
        data.session("GA", customer, 51, "COMPLETED", "1.0", 300L);
        data.session("GA", customer, 52, "COMPLETED", "2.0", 900L);

        call(get("/v1/admin/sessions?country=GA&customerId=" + customer + "&sort=amount:asc"))
                .andExpect(jsonPath("$.items[0].costMinor").value(300));
        call(get("/v1/admin/sessions?country=GA&customerId=" + customer + "&sort=amount:desc"))
                .andExpect(jsonPath("$.items[0].costMinor").value(900));
    }

    @Test
    void theLedgerAndTheActivityOfACustomerAreListedForSupport() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE", marker + "Ledger", "Test", null, Instant.now());
        data.wallet("GA", customer, "XAF", 500, 0);
        TenantContext.runAs("GA", () -> {
            jdbc.update("insert into ledger_entry (id, customer_id, type, amount_minor, idempotency_key, created_at) "
                    + "values (?, ?, 'TOPUP', 500, ?, now())", UUID.randomUUID(), customer, UUID.randomUUID().toString());
            jdbc.update("insert into audit_record (id, actor_id, action, subject, created_at) "
                    + "values (?, ?, 'LOGIN_SUCCEEDED', 'customer', now())", UUID.randomUUID(), customer);
        });
        TenantContext.clear();
        call(post("/v1/admin/customers/GA/" + customer + "/suspend")).andExpect(status().isNoContent());

        call(get("/v1/admin/customers/GA/" + customer + "/ledger")).andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1)).andExpect(jsonPath("$.currency").value("XAF"))
                .andExpect(jsonPath("$.items[0].type").value("TOPUP"));
        call(get("/v1/admin/customers/GA/" + customer + "/activity")).andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.items[0].source").value("ADMIN"))
                .andExpect(jsonPath("$.items[1].action").value("LOGIN_SUCCEEDED"));
        call(get("/v1/admin/customers/GA/" + UUID.randomUUID() + "/ledger")).andExpect(status().isNotFound());
    }
}
