package com.evcharge.customer.charging;

import com.evcharge.customer.FakeOcppServer;
import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import com.evcharge.customer.charging.application.SessionReconciler;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "zella.charging.user-refresh-interval=0s")
@AutoConfigureMockMvc
class EarlyStopRetryIntegrationTest extends PostgresTestBase {

    private static final String START_PATH = "/api/v1/charge-points/CP-14/connectors/1/start";
    private static final String START = "{\"stationId\":\"CP-14\",\"connectorId\":1,\"amountMinor\":5000}";

    private static final FakeOcppServer OCPP = new FakeOcppServer()
            .route("POST", "/api/v1/id-tags", 201, "{\"idTag\":\"x\"}")
            .route("POST", START_PATH, 200, "{\"status\":\"PENDING\"}")
            .station("CP-14");

    @DynamicPropertySource
    static void ocpp(DynamicPropertyRegistry registry) {
        registry.add("zella.ocpp.base-url", OCPP::baseUrl);
    }

    @AfterAll
    static void stop() {
        OCPP.stop();
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    SessionReconciler reconciler;

    @Autowired
    SessionRepository sessions;

    @AfterEach
    void releaseTheConnector() {
        jdbc.update("update funding_hold set state = 'RELEASED' where state = 'OPEN' and session_id in "
                + "(select id from charging_session where station_id = 'CP-14')");
        jdbc.update("update charging_session set state = 'COMPLETED' where station_id = 'CP-14' "
                + "and state in ('START_ACCEPTED', 'ACTIVE')");
    }

    private UUID newCustomer() {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, 'x', 'ACTIVE', now())", id, id + "@example.com");
        jdbc.update("insert into wallet (customer_id, currency, decimals, balance_minor, created_at) "
                + "values (?, 'XOF', 0, 100000, now())", id);
        return id;
    }

    private UUID startCharge(UUID customer) throws Exception {
        String body = mvc.perform(post("/v1/sessions").with(TestTenants.customer(customer))
                        .contentType(MediaType.APPLICATION_JSON).content(START))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(com.jayway.jsonpath.JsonPath.read(body, "$.id"));
    }

    private String refusedByTheCharger(int transaction, String idTag) {
        return "{\"transactionId\":" + transaction + ",\"deviceId\":\"CP-14\",\"connectorId\":1,"
                + "\"status\":\"COMPLETED\",\"startedAt\":\"" + Instant.now().minusSeconds(20)
                + "\",\"stoppedAt\":\"" + Instant.now().minusSeconds(7) + "\",\"energyKwh\":0,"
                + "\"stopReason\":\"Other\",\"idTag\":\"" + idTag + "\"}";
    }

    private String running(int transaction, String idTag) {
        return "{\"transactionId\":" + transaction + ",\"deviceId\":\"CP-14\",\"connectorId\":1,"
                + "\"status\":\"ACTIVE\",\"startedAt\":\"" + Instant.now().minusSeconds(5)
                + "\",\"idTag\":\"" + idTag + "\",\"pricePerKwhMinor\":150,\"currency\":\"XOF\","
                + "\"currencyDecimals\":0}";
    }

    private void platformShows(String... items) {
        OCPP.route("GET", "/api/v1/sessions", 200, "{\"items\":[" + String.join(",", items) + "]}");
    }

    private ChargingSession reconcile(UUID id) {
        return TenantContext.callAs("GA", () -> reconciler.reconcile(sessions.findById(id).orElseThrow()));
    }

    private String idTagOf(UUID customer) {
        return jdbc.queryForObject("select id_tag from customer_id_tag where customer_id = ?", String.class,
                customer);
    }

    @Test
    void aStartTheChargerEndsWithinSecondsIsStartedAgainAndTheNewTransactionIsFollowed() throws Exception {
        UUID customer = newCustomer();
        UUID id = startCharge(customer);
        String tag = idTagOf(customer);
        OCPP.resetHits();
        platformShows(refusedByTheCharger(810, tag));

        ChargingSession restarted = reconcile(id);

        assertThat(restarted.state().name()).isEqualTo("START_ACCEPTED");
        assertThat(restarted.transactionId()).isNull();
        assertThat(OCPP.hits("POST", START_PATH)).isEqualTo(1);

        assertThat(reconcile(id).state().name()).isEqualTo("START_ACCEPTED");
        assertThat(OCPP.hits("POST", START_PATH)).isEqualTo(1);

        platformShows(refusedByTheCharger(810, tag), running(811, tag));

        ChargingSession following = reconcile(id);

        assertThat(following.state().name()).isEqualTo("ACTIVE");
        assertThat(following.transactionId()).isEqualTo(811);
    }

    @Test
    void whenTheChargerKeepsStoppingTheChargeFailsClearlyAndTheHoldIsReleased() throws Exception {
        UUID customer = newCustomer();
        UUID id = startCharge(customer);
        String tag = idTagOf(customer);
        platformShows(refusedByTheCharger(820, tag));
        assertThat(reconcile(id).state().name()).isEqualTo("START_ACCEPTED");
        platformShows(refusedByTheCharger(820, tag), refusedByTheCharger(821, tag));
        assertThat(reconcile(id).state().name()).isEqualTo("START_ACCEPTED");
        platformShows(refusedByTheCharger(820, tag), refusedByTheCharger(821, tag), refusedByTheCharger(822, tag));

        ChargingSession last = reconcile(id);

        assertThat(last.state().name()).isEqualTo("FAILED");
        assertThat(last.failureCode()).isEqualTo("CHARGER_STOPPED_EARLY");
        assertThat(jdbc.queryForObject("select state from funding_hold where session_id = ?", String.class, id))
                .isEqualTo("RELEASED");
    }
}
