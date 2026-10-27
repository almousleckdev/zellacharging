package com.evcharge.customer.admin;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.application.port.AdminTokenIssuer;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.shared.TenantContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminOverviewIntegrationTest extends PostgresTestBase {

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

    @BeforeEach
    void prepare() {
        fixtures = new AdminFixtures(admins, passwords, tokens);
        data = new AdminTestData(jdbc);
        TenantContext.clear();
    }

    private String json(String token, String country) throws Exception {
        String url = country == null ? "/v1/admin/overview" : "/v1/admin/overview?country=" + country;
        return mvc.perform(get(url).header("Authorization", "Bearer " + token)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private Number figure(String json, String code, String path) {
        JSONArray found = JsonPath.read(json, "$.countries[?(@.code=='" + code + "')]" + path);
        return (Number) found.getFirst();
    }

    @Test
    void aSuperAdminSeesEachCountryWithItsOwnFiguresAndNothingLeaksBetweenThem() throws Exception {
        String root = fixtures.create(AdminRole.SUPER_ADMIN, Set.of()).accessToken();
        String before = json(root, "ALL");

        UUID active = data.customer("GA", "ACTIVE");
        UUID blocked = data.customer("GA", "BLOCKED");
        data.wallet("GA", active, "XAF", 5000, 1000);
        data.session("GA", active, 1, "COMPLETED", "12.500", 1875L);
        data.session("GA", blocked, 2, "ACTIVE", null, null);
        data.topup("GA", active, "SUCCEEDED", 5000);
        data.topup("GA", active, "PENDING", 2000);
        data.withdrawal("GA", active, "PAID", 3000);
        data.withdrawal("GA", active, "REQUESTED", 1000);
        data.wallet("ML", data.customer("ML", "ACTIVE"), "XOF", 700, 0);

        String after = json(root, "ALL");

        assertThat(after).contains("\"scope\":\"ALL\"");
        assertThat(figure(after, "GA", ".customers.total").longValue() - figure(before, "GA", ".customers.total").longValue()).isEqualTo(2);
        assertThat(figure(after, "GA", ".customers.active").longValue() - figure(before, "GA", ".customers.active").longValue()).isEqualTo(1);
        assertThat(figure(after, "GA", ".wallets.balanceMinor").longValue() - figure(before, "GA", ".wallets.balanceMinor").longValue()).isEqualTo(5000);
        assertThat(figure(after, "GA", ".wallets.reservedMinor").longValue() - figure(before, "GA", ".wallets.reservedMinor").longValue()).isEqualTo(1000);
        assertThat(figure(after, "GA", ".sessions.completed").longValue() - figure(before, "GA", ".sessions.completed").longValue()).isEqualTo(1);
        assertThat(figure(after, "GA", ".sessions.live").longValue() - figure(before, "GA", ".sessions.live").longValue()).isEqualTo(1);
        assertThat(figure(after, "GA", ".sessions.revenueMinor").longValue() - figure(before, "GA", ".sessions.revenueMinor").longValue()).isEqualTo(1875);
        assertThat(figure(after, "GA", ".sessions.energyKwh").doubleValue() - figure(before, "GA", ".sessions.energyKwh").doubleValue()).isEqualTo(12.5);
        assertThat(figure(after, "GA", ".wallets.topupsSucceededMinor").longValue() - figure(before, "GA", ".wallets.topupsSucceededMinor").longValue()).isEqualTo(5000);
        assertThat(figure(after, "GA", ".wallets.pendingTopups").longValue() - figure(before, "GA", ".wallets.pendingTopups").longValue()).isEqualTo(1);
        assertThat(figure(after, "GA", ".wallets.withdrawalsPaidMinor").longValue() - figure(before, "GA", ".wallets.withdrawalsPaidMinor").longValue()).isEqualTo(3000);
        assertThat(figure(after, "GA", ".wallets.pendingWithdrawals").longValue() - figure(before, "GA", ".wallets.pendingWithdrawals").longValue()).isEqualTo(1);
        assertThat(figure(after, "ML", ".customers.total").longValue() - figure(before, "ML", ".customers.total").longValue()).isEqualTo(1);
        assertThat(figure(after, "ML", ".wallets.balanceMinor").longValue() - figure(before, "ML", ".wallets.balanceMinor").longValue()).isEqualTo(700);
        assertThat(figure(after, "ML", ".sessions.completed").longValue()).isEqualTo(figure(before, "ML", ".sessions.completed").longValue());
    }

    @Test
    void aCountryScopeReturnsOnlyThatCountry() throws Exception {
        String root = fixtures.create(AdminRole.SUPER_ADMIN, Set.of()).accessToken();

        mvc.perform(get("/v1/admin/overview?country=ML").header("Authorization", "Bearer " + root))
                .andExpect(status().isOk()).andExpect(jsonPath("$.scope").value("ML"))
                .andExpect(jsonPath("$.countries.length()").value(1)).andExpect(jsonPath("$.countries[0].code").value("ML"))
                .andExpect(jsonPath("$.countries[0].currency").value("XOF"));
    }

    @Test
    void aCountryAdminNeverGetsAnotherCountry() throws Exception {
        String gabon = fixtures.create(AdminRole.ADMIN, Set.of("GA")).accessToken();

        mvc.perform(get("/v1/admin/overview").header("Authorization", "Bearer " + gabon))
                .andExpect(status().isOk()).andExpect(jsonPath("$.countries.length()").value(1))
                .andExpect(jsonPath("$.countries[0].code").value("GA"));
        mvc.perform(get("/v1/admin/overview?country=ALL").header("Authorization", "Bearer " + gabon))
                .andExpect(status().isOk()).andExpect(jsonPath("$.countries.length()").value(1));
        mvc.perform(get("/v1/admin/overview?country=ML").header("Authorization", "Bearer " + gabon))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("COUNTRY_FORBIDDEN"));
    }

    @Test
    void anonymousAndCustomerCallersAreRejected() throws Exception {
        mvc.perform(get("/v1/admin/overview")).andExpect(status().isUnauthorized());
        mvc.perform(get("/v1/admin/overview").with(TestTenants.anyCustomer())).andExpect(status().isUnauthorized());
    }
}
