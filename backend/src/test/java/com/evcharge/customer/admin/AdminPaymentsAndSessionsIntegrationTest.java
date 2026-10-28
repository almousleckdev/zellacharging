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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminPaymentsAndSessionsIntegrationTest extends PostgresTestBase {

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
    private String marker;
    private String root;

    @BeforeEach
    void prepare() {
        fixtures = new AdminFixtures(admins, passwords, tokens);
        data = new AdminTestData(jdbc);
        marker = "Pq" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        root = fixtures.create(AdminRole.SUPER_ADMIN, Set.of()).accessToken();
        TenantContext.clear();
    }

    private ResultActions call(String token, String path) throws Exception {
        return mvc.perform(get(path).header("Authorization", "Bearer " + token));
    }

    @Test
    void paymentsListTopupsAndWithdrawalsOfTheMatchingCustomerNewestFirst() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE", marker, "Obame", null, Instant.now());
        data.wallet("GA", customer, "XAF", 4000, 0);
        data.topup("GA", customer, "SUCCEEDED", 5000);
        data.withdrawal("GA", customer, "PAID", 1000);

        call(root, "/v1/admin/payments?country=GA&q=" + marker)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.items[0].kind").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.items[0].currency").value("XAF"))
                .andExpect(jsonPath("$.items[1].kind").value("TOPUP"))
                .andExpect(jsonPath("$.items[1].amountMinor").value(5000))
                .andExpect(jsonPath("$.items[1].customerName").value(marker + " Obame"));
        call(root, "/v1/admin/payments?country=GA&kind=TOPUP&q=" + marker)
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void sessionsListTheCustomerStationAndBatteryLevels() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE", marker, "Nze", null, Instant.now());
        data.session("GA", customer, 1, "COMPLETED", "0.385", 138L);

        call(root, "/v1/admin/sessions?country=GA&state=COMPLETED&q=" + marker)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].customerName").value(marker + " Nze"))
                .andExpect(jsonPath("$.items[0].costMinor").value(138));
    }

    @Test
    void anAdminWithoutThePermissionIsRefused() throws Exception {
        String support = fixtures.create(AdminRole.SUPPORT, Set.of("GA")).accessToken();
        TenantContext.clear();

        call(support, "/v1/admin/payments?country=GA").andExpect(status().isForbidden());
        call(support, "/v1/admin/sessions?country=GA").andExpect(status().isOk());
    }

    @Test
    void sandboxTopupsNeverAppearInPayments() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE", marker, "Test", null, Instant.now());
        data.wallet("GA", customer, "XAF", 0, 0);
        data.topup("GA", customer, "SUCCEEDED", 19151);
        TenantContext.runAs("GA", () -> jdbc.update(
                "update topup set provider_ref = 'sandbox-GAXXXX' where customer_id = ?", customer));

        call(root, "/v1/admin/payments?country=GA&q=" + marker).andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void commissionsListTheServiceFeeOfEveryBilledSession() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE", marker, "Fee", null, Instant.now());
        data.session("GA", customer, 1, "COMPLETED", "0.385", 1000L);

        call(root, "/v1/admin/commissions?country=GA")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summaries[0].sessions").isNumber())
                .andExpect(jsonPath("$.items[?(@.customerName=='" + marker + " Fee')].electricMinor").value(1000));
    }
}
