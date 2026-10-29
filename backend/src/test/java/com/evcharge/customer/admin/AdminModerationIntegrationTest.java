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

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminModerationIntegrationTest extends PostgresTestBase {

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

    private String statusOf(UUID customer) {
        return TenantContext.callAs("GA", () -> jdbc.queryForObject("select status from customer where id = ?",
                String.class, customer));
    }

    @Test
    void anAdminSuspendsThenReactivatesACustomerAndEachStepIsAudited() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE");

        call(post("/v1/admin/customers/GA/" + customer + "/suspend"), root).andExpect(status().isNoContent());
        assertThat(statusOf(customer)).isEqualTo("BLOCKED");
        call(post("/v1/admin/customers/GA/" + customer + "/suspend"), root).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CUSTOMER_ALREADY_SUSPENDED"));

        call(post("/v1/admin/customers/GA/" + customer + "/reactivate"), root).andExpect(status().isNoContent());
        assertThat(statusOf(customer)).isEqualTo("ACTIVE");
        call(post("/v1/admin/customers/GA/" + customer + "/reactivate"), root).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CUSTOMER_NOT_SUSPENDED"));

        call(get("/v1/admin/audit?country=GA&q=" + customer), root).andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2));
    }

    @Test
    void deletingIsRefusedWhileTheWalletHoldsMoneyAndAllowedOnceItIsEmpty() throws Exception {
        UUID funded = data.customer("GA", "ACTIVE");
        data.wallet("GA", funded, "XAF", 500, 0);
        UUID empty = data.customer("GA", "ACTIVE");
        data.wallet("GA", empty, "XAF", 0, 0);

        call(delete("/v1/admin/customers/GA/" + funded), root).andExpect(status().isConflict());
        assertThat(statusOf(funded)).isEqualTo("ACTIVE");
        call(delete("/v1/admin/customers/GA/" + empty), root).andExpect(status().isNoContent());
        assertThat(statusOf(empty)).isEqualTo("DELETED");
        call(get("/v1/admin/customers/GA/" + empty), root).andExpect(status().isNotFound());
    }

    @Test
    void anAdminWithoutTheManagePermissionCannotModerate() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE");
        String support = fixtures.create(AdminRole.SUPPORT, Set.of("GA")).accessToken();

        call(post("/v1/admin/customers/GA/" + customer + "/suspend"), support).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PERMISSION_DENIED"));
        assertThat(statusOf(customer)).isEqualTo("ACTIVE");
    }

    @Test
    void sessionsCanBeFilteredByCustomerAndOpenedInDetail() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE", "Zq", "History", null, java.time.Instant.now());
        data.session("GA", customer, 21, "COMPLETED", "2.5", 900L);
        data.session("GA", customer, 22, "FAILED", null, null);

        String id = TenantContext.callAs("GA", () -> jdbc.queryForObject(
                "select id::text from charging_session where customer_id = ? and state = 'COMPLETED'", String.class,
                customer));
        call(get("/v1/admin/sessions?country=GA&customerId=" + customer), root).andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2));
        call(get("/v1/admin/sessions/GA/" + id), root).andExpect(status().isOk())
                .andExpect(jsonPath("$.session.customerName").value("Zq History"))
                .andExpect(jsonPath("$.serviceFeeMinor").isNumber());
        call(get("/v1/admin/sessions/GA/" + UUID.randomUUID()), root).andExpect(status().isNotFound());
    }

    @Test
    void failedSessionsSurfaceAsIncidentsThatCanBeAcknowledged() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE", "Zq", "Incident", null, java.time.Instant.now());
        data.session("GA", customer, 31, "FAILED", null, null);

        call(get("/v1/admin/incidents?country=GA&status=OPEN&kind=SESSION_FAILED"), root).andExpect(status().isOk())
                .andExpect(jsonPath("$.open").isNumber());
        String key = TenantContext.callAs("GA", () -> "SESSION_FAILED:" + jdbc.queryForObject(
                "select id::text from charging_session where customer_id = ?", String.class, customer));
        call(post("/v1/admin/incidents/GA/review").contentType("application/json")
                .content("{\"key\":\"" + key + "\",\"status\":\"RESOLVED\",\"note\":\"checked\"}"), root)
                .andExpect(status().isNoContent());
        call(get("/v1/admin/incidents?country=GA&status=RESOLVED&customerId=" + customer), root)
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[?(@.key=='" + key + "')].note")
                        .value("checked"));
    }

    @Test
    void theSeriesTariffsAndSystemEndpointsAnswer() throws Exception {
        call(get("/v1/admin/series?country=GA&bucket=day"), root).andExpect(status().isOk())
                .andExpect(jsonPath("$.countries[0].code").value("GA"));
        call(get("/v1/admin/series?country=GA&bucket=year"), root).andExpect(status().isBadRequest());
        call(get("/v1/admin/tariffs?country=GA"), root).andExpect(status().isOk())
                .andExpect(jsonPath("$.settings[0].section").value("charging"));
        call(get("/v1/admin/system"), root).andExpect(status().isOk())
                .andExpect(jsonPath("$.probes[?(@.name=='database')].result.up").value(true));
    }
}
