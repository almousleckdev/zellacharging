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

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminWithdrawalApprovalIntegrationTest extends PostgresTestBase {

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
    private UUID customer;
    private UUID withdrawal;

    @BeforeEach
    void prepare() {
        fixtures = new AdminFixtures(admins, passwords, tokens);
        data = new AdminTestData(jdbc);
        root = fixtures.create(AdminRole.SUPER_ADMIN, Set.of()).accessToken();
        customer = data.customer("GA", "ACTIVE", "Wd" + UUID.randomUUID().toString().substring(0, 8), "Obame", null,
                Instant.now());
        data.wallet("GA", customer, "XAF", 5000, 0);
        withdrawal = UUID.randomUUID();
        TenantContext.runAs("GA", () -> jdbc.update("insert into withdrawal (id, customer_id, operator, msisdn, "
                        + "amount_minor, state, reference, idempotency_key, created_at, updated_at) values "
                        + "(?, ?, 'MOOV', '+24162000000', 1000, 'PENDING_APPROVAL', ?, ?, now(), now())", withdrawal,
                customer, UUID.randomUUID().toString(), UUID.randomUUID().toString()));
        TenantContext.clear();
    }

    private ResultActions review(String token, String action, String body) throws Exception {
        return mvc.perform(post("/v1/admin/withdrawals/GA/" + withdrawal + "/" + action)
                .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private String stateOfWithdrawal() {
        return TenantContext.callAs("GA", () -> jdbc.queryForObject("select state from withdrawal where id = ?",
                String.class, withdrawal));
    }

    @Test
    void rejectingReturnsTheMoneyRecordsTheReasonAndIsAudited() throws Exception {
        review(root, "reject", "{\"note\":\"Suspicious activity\"}").andExpect(status().isNoContent());

        assertThat(stateOfWithdrawal()).isEqualTo("FAILED");
        assertThat(TenantContext.callAs("GA", () -> jdbc.queryForObject(
                "select balance_minor from wallet where customer_id = ?", Long.class, customer))).isEqualTo(6000);
        assertThat(jdbc.queryForObject("select count(*) from admin_audit_record where action = 'WITHDRAWAL_REJECTED' "
                + "and subject = ?", Integer.class, withdrawal.toString())).isOne();
    }

    @Test
    void aSecondDecisionIsRefused() throws Exception {
        review(root, "reject", "{\"note\":\"No\"}").andExpect(status().isNoContent());

        review(root, "reject", "{\"note\":\"Again\"}").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WITHDRAWAL_NOT_PENDING"));
    }

    @Test
    void rejectingNeedsAReason() throws Exception {
        review(root, "reject", "{\"note\":\" \"}").andExpect(status().isBadRequest());

        assertThat(stateOfWithdrawal()).isEqualTo("PENDING_APPROVAL");
    }

    @Test
    void approvingWithoutAConfiguredPayoutLeavesItPending() throws Exception {
        review(root, "approve", "{}").andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("WITHDRAWALS_UNAVAILABLE"));

        assertThat(stateOfWithdrawal()).isEqualTo("PENDING_APPROVAL");
    }

    @Test
    void onlyAdminsWithTheReviewPermissionCanDecide() throws Exception {
        String finance = fixtures.create(AdminRole.FINANCE, Set.of("GA")).accessToken();
        String support = fixtures.create(AdminRole.SUPPORT, Set.of("GA")).accessToken();
        String admin = fixtures.create(AdminRole.ADMIN, Set.of("GA")).accessToken();

        review(finance, "reject", "{\"note\":\"x\"}").andExpect(status().isForbidden());
        review(support, "reject", "{\"note\":\"x\"}").andExpect(status().isForbidden());
        assertThat(stateOfWithdrawal()).isEqualTo("PENDING_APPROVAL");
        review(admin, "reject", "{\"note\":\"ok\"}").andExpect(status().isNoContent());
    }

    @Test
    void anonymousCallersAreRefused() throws Exception {
        mvc.perform(post("/v1/admin/withdrawals/GA/" + withdrawal + "/approve")).andExpect(status().isUnauthorized());
    }
}
