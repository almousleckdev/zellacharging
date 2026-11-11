package com.evcharge.customer.partner;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SuspensionNotificationIntegrationTest extends PostgresTestBase {

    private static final String BODY = "{\"owedMinor\":69000,\"currency\":\"XAF\",\"decimals\":0}";

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PartnerKeys keys;

    private PartnerFixtures fixtures;
    private String key;
    private UUID driver;
    private String driverNumber;

    @BeforeEach
    void prepare() throws Exception {
        fixtures = new PartnerFixtures(jdbc, keys);
        key = fixtures.keyFor("GA");
        driverNumber = "+2417" + (1000000 + java.util.concurrent.ThreadLocalRandom.current().nextInt(9000000));
        driver = fixtures.customer("GA", "A", "B", UUID.randomUUID() + "@example.com", driverNumber, "ACTIVE");
        fixtures.link("GA", driver);
        TenantContext.runAs("GA", () -> jdbc.update("insert into device_token (token, customer_id, platform, "
                + "created_at, last_seen_at) values (?, ?, 'ANDROID', now(), now())", "tok-" + driver, driver));
    }

    private ResultActions send(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.header("X-Api-Key", key));
    }

    private void suspend() throws Exception {
        send(put("/v1/partners/ndzela/customers/" + driver + "/suspension").contentType(MediaType.APPLICATION_JSON)
                .content(BODY)).andExpect(status().isNoContent());
    }

    private int notices(UUID customer, String type) {
        return TenantContext.callAs("GA", () -> jdbc.queryForObject("select count(*) from notification_outbox "
                + "where customer_id = ? and type = ?", Integer.class, customer, type));
    }

    private void topUpFrom(UUID helper, String msisdn) throws Exception {
        mvc.perform(post("/v1/wallet/topups").with(TestTenants.customer(helper)).contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .content("{\"amountMinor\":5000,\"msisdn\":\"" + msisdn + "\",\"operator\":\"AIRTEL\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void suspendingQueuesAPushAndKeepsTheDeviceToken() throws Exception {
        suspend();

        assertThat(notices(driver, "ACCOUNT_SUSPENDED_ARREARS")).isEqualTo(1);
        assertThat(TenantContext.callAs("GA", () -> jdbc.queryForObject(
                "select count(*) from device_token where customer_id = ?", Integer.class, driver))).isEqualTo(1);
    }

    @Test
    void suspendingTwiceQueuesOnePush() throws Exception {
        suspend();
        suspend();

        assertThat(notices(driver, "ACCOUNT_SUSPENDED_ARREARS")).isEqualTo(1);
    }

    @Test
    void restoringQueuesARestoredPush() throws Exception {
        suspend();

        send(delete("/v1/partners/ndzela/customers/" + driver + "/suspension")).andExpect(status().isNoContent());

        assertThat(notices(driver, "ACCOUNT_RESTORED")).isEqualTo(1);
    }

    @Test
    void anExactNumberMatchWarnsTheDriverAndAPlainHelperGetsNothing() throws Exception {
        suspend();
        UUID plainHelper = fixtures.customer("GA", "A", "B", UUID.randomUUID() + "@example.com", null, "ACTIVE");

        topUpFrom(plainHelper, driverNumber);

        assertThat(notices(driver, "SUSPENDED_HELP_DETECTED")).isEqualTo(1);
        assertThat(notices(plainHelper, "HELPER_WARNED")).isZero();
    }

    @Test
    void aLinkedHelperIsWarnedToo() throws Exception {
        suspend();
        UUID linkedHelper = fixtures.customer("GA", "A", "B", UUID.randomUUID() + "@example.com", null, "ACTIVE");
        fixtures.link("GA", linkedHelper);

        topUpFrom(linkedHelper, driverNumber);

        assertThat(notices(linkedHelper, "HELPER_WARNED")).isEqualTo(1);
        assertThat(notices(driver, "SUSPENDED_HELP_DETECTED")).isEqualTo(1);
    }
}
