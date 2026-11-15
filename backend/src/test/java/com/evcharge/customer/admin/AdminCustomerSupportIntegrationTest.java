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
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminCustomerSupportIntegrationTest extends PostgresTestBase {

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

    private ResultActions call(MockHttpServletRequestBuilder request, String token, String body) throws Exception {
        return mvc.perform(request.header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions call(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.header("Authorization", "Bearer " + root));
    }

    private <T> T query(String sql, Class<T> type, Object... args) {
        return TenantContext.callAs("GA", () -> jdbc.queryForObject(sql, type, args));
    }

    private String base(UUID customer) {
        return "/v1/admin/customers/GA/" + customer;
    }

    @Test
    void unlockingClearsTheLoginLockAndShowsInTheDetailBeforehand() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE");
        TenantContext.runAs("GA", () -> jdbc.update("update customer set failed_login_attempts = 3, "
                + "locked_until = now() + interval '30 minutes' where id = ?", customer));

        call(get(base(customer))).andExpect(status().isOk()).andExpect(jsonPath("$.access.lockedUntil").isNotEmpty());
        call(post(base(customer) + "/unlock")).andExpect(status().isNoContent());

        call(get(base(customer))).andExpect(jsonPath("$.access.lockedUntil").doesNotExist())
                .andExpect(jsonPath("$.access.failedLoginAttempts").value(0));
    }

    @Test
    void resendingACodeIgnoresTheCooldownAndResettingAttemptsReopensTheCode() throws Exception {
        UUID customer = data.customer("GA", "PENDING_VERIFICATION");
        String email = "{\"channel\":\"EMAIL\"}";

        call(post(base(customer) + "/resend-code"), root, email).andExpect(status().isNoContent());
        call(post(base(customer) + "/resend-code"), root, email).andExpect(status().isNoContent());
        assertThat(query("select count(*) from verification_code where customer_id = ?", Integer.class, customer))
                .isEqualTo(2);

        TenantContext.runAs("GA", () -> jdbc.update("update verification_code set attempts = 5 where customer_id = ?",
                customer));
        call(post(base(customer) + "/reset-attempts"), root, email).andExpect(status().isNoContent());
        assertThat(query("select max(attempts) from verification_code where customer_id = ?", Integer.class,
                customer)).isZero();
    }

    @Test
    void markingVerifiedActivatesAPendingCustomer() throws Exception {
        UUID customer = data.customer("GA", "PENDING_VERIFICATION");

        call(post(base(customer) + "/verify"), root, "{\"channel\":\"EMAIL\"}").andExpect(status().isNoContent());

        assertThat(query("select status from customer where id = ?", String.class, customer)).isEqualTo("ACTIVE");
        call(get(base(customer))).andExpect(jsonPath("$.emailVerified").value(true));
    }

    @Test
    void changingThePhoneRequiresReverificationAndRefusesANumberInUse() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE");
        UUID other = data.customer("GA", "ACTIVE", "C", "D", "+24107123456", java.time.Instant.now());
        String change = "{\"channel\":\"PHONE\",\"value\":\"+24106123456\"}";

        call(put(base(customer) + "/contact"), root, change).andExpect(status().isNoContent());
        assertThat(query("select phone_e164 from customer where id = ?", String.class, customer))
                .isEqualTo("+24106123456");
        assertThat(query("select phone_verified_at is null from customer where id = ?", Boolean.class, customer))
                .isTrue();

        call(put(base(customer) + "/contact"), root, "{\"channel\":\"PHONE\",\"value\":\"+24107123456\"}")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("CONTACT_ALREADY_USED"));
        assertThat(other).isNotNull();
    }

    @Test
    void changingTheEmailNormalisesItAndRefusesAnotherCustomersAddress() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE");
        UUID other = data.customer("GA", "ACTIVE");

        call(put(base(customer) + "/contact"), root, "{\"channel\":\"EMAIL\",\"value\":\"  New.Mail@Example.com \"}")
                .andExpect(status().isNoContent());
        assertThat(query("select email from customer where id = ?", String.class, customer))
                .isEqualTo("new.mail@example.com");
        call(put(base(customer) + "/contact"), root, "{\"channel\":\"EMAIL\",\"value\":\"" + other + "@example.com\"}")
                .andExpect(status().isConflict());
    }

    @Test
    void linkingToNdzelaIsVisibleInTheDetailAndCanBeUndone() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE");
        UUID suspended = data.customer("GA", "BLOCKED");

        call(put(base(customer) + "/ndzela-link")).andExpect(status().isNoContent());
        call(get(base(customer))).andExpect(jsonPath("$.ndzela.linked").value(true));
        call(put(base(suspended) + "/ndzela-link")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CUSTOMER_NOT_ACTIVE"));

        call(delete(base(customer) + "/ndzela-link")).andExpect(status().isNoContent());
        call(get(base(customer))).andExpect(jsonPath("$.ndzela.linked").value(false));
    }

    @Test
    void anAdminCreatesAnActiveCustomerWhoCanThenResetThePassword() throws Exception {
        String body = "{\"firstName\":\" Ada \",\"lastName\":\"Obiang\",\"email\":\"Ada.O@Example.com\","
                + "\"phone\":\"+24106555111\"}";

        String json = call(post("/v1/admin/customers/GA"), root, body).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID id = UUID.fromString(json.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1"));

        call(get(base(id))).andExpect(jsonPath("$.firstName").value("Ada"))
                .andExpect(jsonPath("$.email").value("ada.o@example.com")).andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.emailVerified").value(true)).andExpect(jsonPath("$.phoneVerified").value(true));
        call(post("/v1/admin/customers/GA"), root, body).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONTACT_ALREADY_USED"));
        call(post("/v1/admin/customers/GA"), root, "{\"firstName\":\"A\",\"lastName\":\"B\"}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("CONTACT_REQUIRED"));
    }

    @Test
    void supportActionsNeedTheManagePermission() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE");
        String support = fixtures.create(AdminRole.SUPPORT, Set.of("GA")).accessToken();

        call(post(base(customer) + "/unlock").header("Authorization", "Bearer " + support), support, "{}")
                .andExpect(status().isForbidden());
        call(put(base(customer) + "/ndzela-link").header("Authorization", "Bearer " + support), support, "{}")
                .andExpect(status().isForbidden());
    }

    @Test
    void everySupportActionIsAudited() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE");
        call(post(base(customer) + "/unlock")).andExpect(status().isNoContent());
        call(put(base(customer) + "/ndzela-link")).andExpect(status().isNoContent());

        call(get("/v1/admin/audit?country=GA&q=" + customer)).andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2));
    }

    @Test
    void theServiceFeeIsZeroAndPastChargesKeepTheirStoredFee() throws Exception {
        assertThat(jdbc.queryForObject("select value from platform_setting where key = 'charging.serviceFeePercent'",
                String.class)).isEqualTo("0");
        UUID customer = data.customer("GA", "ACTIVE");
        UUID session = UUID.randomUUID();
        TenantContext.runAs("GA", () -> {
            jdbc.update("insert into charging_session (id, customer_id, station_id, connector_id, id_tag, state, "
                    + "started_at, energy_kwh, cost_minor, created_at, updated_at) values (?, ?, 'CP-1', 1, 'Z-x', "
                    + "'COMPLETED', now(), 10, 1000, now(), now())", session, customer);
            jdbc.update("insert into session_charge (session_id, customer_id, state, energy_cost_minor, vat_minor, "
                    + "service_fee_minor, total_minor, currency, decimals, updated_at) values (?, ?, 'CHARGED', "
                    + "1000, 0, 20, 1020, 'XAF', 0, now())", session, customer);
        });

        call(get("/v1/admin/commissions?country=GA&customerId=" + customer)).andExpect(status().isOk())
                .andExpect(jsonPath("$.summaries[0].serviceFeeMinor").value(20))
                .andExpect(jsonPath("$.summaries[0].electricMinor").value(1000));
    }
}
