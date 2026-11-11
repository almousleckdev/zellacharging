package com.evcharge.customer.wallet;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import com.evcharge.customer.partner.PartnerFixtures;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SuspendedNumberTopupTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    private PartnerFixtures fixtures;
    private UUID suspended;
    private String suspendedNumber;

    @BeforeEach
    void prepare() {
        fixtures = new PartnerFixtures(jdbc);
        suspended = customer();
        suspendedNumber = "+2417" + (1000000 + java.util.concurrent.ThreadLocalRandom.current().nextInt(9000000));
        fixtures.suspend(TENANT, suspended, 69000, suspendedNumber);
    }

    private UUID customer() {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, 'x', 'ACTIVE', now())", id, id + "@example.com");
        return id;
    }

    private ResultActions topUp(UUID customer, String msisdn, String key) throws Exception {
        String body = "{\"amountMinor\":5000,\"msisdn\":\"" + msisdn + "\",\"operator\":\"AIRTEL\"}";
        return mvc.perform(post("/v1/wallet/topups").with(TestTenants.customer(customer))
                .contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key", key).content(body));
    }

    private int evidence(UUID receiving) {
        return TenantContext.callAs(TENANT, () -> jdbc.queryForObject("select count(*) from suspension_evidence "
                + "where suspended_customer_id = ? and receiving_customer_id = ?", Integer.class, suspended,
                receiving));
    }

    private int topups(UUID customer) {
        return jdbc.queryForObject("select count(*) from topup where customer_id = ?", Integer.class, customer);
    }

    @Test
    void aTopUpPaidFromASuspendedDriversNumberIsRefusedAndRecorded() throws Exception {
        UUID helper = customer();

        topUp(helper, suspendedNumber, "k-1").andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("TOPUP_NUMBER_SUSPENDED"));

        assertThat(topups(helper)).isZero();
        assertThat(evidence(helper)).isEqualTo(1);
    }

    @Test
    void retryingTheSameKeyRecordsOneEvent() throws Exception {
        UUID helper = customer();

        topUp(helper, suspendedNumber, "k-2").andExpect(status().isUnprocessableEntity());
        topUp(helper, suspendedNumber, "k-2").andExpect(status().isUnprocessableEntity());

        assertThat(evidence(helper)).isEqualTo(1);
    }

    @Test
    void aNormalNumberIsUntouched() throws Exception {
        UUID helper = customer();

        topUp(helper, "+24177999002", "k-3").andExpect(status().isCreated());

        assertThat(evidence(helper)).isZero();
    }

    @Test
    void theEventKnowsWhetherTheReceivingAccountIsAnNdzelaDriver() throws Exception {
        UUID linkedHelper = customer();
        UUID plainHelper = customer();
        fixtures.link(TENANT, linkedHelper);

        topUp(linkedHelper, suspendedNumber, "k-4").andExpect(status().isUnprocessableEntity());
        topUp(plainHelper, suspendedNumber, "k-5").andExpect(status().isUnprocessableEntity());

        assertThat(linkedFlag(linkedHelper)).isTrue();
        assertThat(linkedFlag(plainHelper)).isFalse();
    }

    private boolean linkedFlag(UUID receiving) {
        return TenantContext.callAs(TENANT, () -> jdbc.queryForObject("select receiving_linked "
                + "from suspension_evidence where receiving_customer_id = ?", Boolean.class, receiving));
    }
}
