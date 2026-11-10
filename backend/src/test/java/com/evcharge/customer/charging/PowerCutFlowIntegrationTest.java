package com.evcharge.customer.charging;

import com.evcharge.customer.FakeOcppServer;
import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import com.evcharge.customer.charging.application.StalledSessionCloser;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PowerCutFlowIntegrationTest extends PostgresTestBase {

    private static final long WALLET = 100000;
    private static final String START = "{\"stationId\":\"CP-5\",\"connectorId\":1,\"amountMinor\":5000}";

    private static final FakeOcppServer OCPP = new FakeOcppServer()
            .route("POST", "/api/v1/id-tags", 201, "{\"idTag\":\"x\"}")
            .route("POST", "/api/v1/charge-points/CP-5/connectors/1/start", 200, "{\"status\":\"PENDING\"}")
            .route("POST", "/api/v1/charge-points/CP-5/connectors/1/stop", 502, "{}")
            .station("CP-5");

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
    StalledSessionCloser closer;

    @Autowired
    SessionRepository sessions;

    private UUID newCustomer() {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, 'x', 'ACTIVE', now())", id, id + "@example.com");
        jdbc.update("insert into wallet (customer_id, currency, decimals, balance_minor, created_at) "
                + "values (?, 'XOF', 0, ?, now())", id, WALLET);
        return id;
    }

    private ResultActions start(UUID customer) throws Exception {
        return mvc.perform(post("/v1/sessions").with(TestTenants.customer(customer))
                .contentType(MediaType.APPLICATION_JSON).content(START));
    }

    private static final java.util.concurrent.atomic.AtomicInteger TRANSACTIONS =
            new java.util.concurrent.atomic.AtomicInteger(700);

    private void platformShowsTheChargeRunning(String idTag) {
        int transaction = TRANSACTIONS.incrementAndGet();
        OCPP.route("GET", "/api/v1/sessions/" + transaction + "/metering", 200,
                "{\"totalKwh\":2.0,\"currentPowerKw\":7.0,\"voltage\":230,"
                        + "\"sampledAt\":\"2026-11-09T09:00:00Z\"}");
        OCPP.route("GET", "/api/v1/sessions", 200, "{\"items\":[{\"transactionId\":" + transaction + ",\"deviceId\":\"CP-5\","
                + "\"connectorId\":1,\"status\":\"ACTIVE\",\"startedAt\":\"" + Instant.now().minusSeconds(120)
                + "\",\"idTag\":\"" + idTag + "\",\"pricePerKwhMinor\":150,\"currency\":\"XOF\","
                + "\"currencyDecimals\":0}]}");
    }

    @Test
    void whenTheChargerGoesSilentTheChargeStopsAndOnlyTheEnergyDeliveredIsPaid() throws Exception {
        UUID customer = newCustomer();
        String sessionId = com.jayway.jsonpath.JsonPath.read(
                start(customer).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.id");
        platformShowsTheChargeRunning(jdbc.queryForObject(
                "select id_tag from customer_id_tag where customer_id = ?", String.class, customer));
        mvc.perform(get("/v1/sessions/active").with(TestTenants.customer(customer)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.state").value("ACTIVE"));

        jdbc.update("update charging_session set reading_at = now() - interval '5 minutes' where id = ?::uuid",
                sessionId);
        TenantContext.runAs("GA", () -> closer.closeIfStalled(
                sessions.findById(UUID.fromString(sessionId)).orElseThrow()));

        mvc.perform(get("/v1/sessions/" + sessionId).with(TestTenants.customer(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("ABANDONED"))
                .andExpect(jsonPath("$.failureCode").value("CHARGER_OFFLINE"))
                .andExpect(jsonPath("$.stopReason").value("PowerLoss"))
                .andExpect(jsonPath("$.energyKwh").value(2.0));
        Long balance = jdbc.queryForObject("select balance_minor from wallet where customer_id = ?", Long.class,
                customer);
        assertThat(WALLET - balance).isPositive().isLessThan(1000);
        mvc.perform(get("/v1/sessions/active").with(TestTenants.customer(customer))).andExpect(status().isNoContent());
    }

    @Test
    void afterTheCutTheCustomerCanScanAgainEvenIfThePlatformStillHoldsTheOldIdTag() throws Exception {
        UUID customer = newCustomer();
        start(customer).andExpect(status().isCreated());
        String oldTag = jdbc.queryForObject("select id_tag from customer_id_tag where customer_id = ?", String.class,
                customer);
        jdbc.update("update charging_session set state = 'ABANDONED' where customer_id = ?", customer);
        OCPP.route("POST", "/api/v1/charge-points/CP-5/connectors/1/start", 409,
                "{\"status\":\"REJECTED\",\"reasonCode\":\"CONCURRENT_IDTAG\"}");
        OCPP.resetHits();

        start(customer).andExpect(status().isCreated()).andExpect(jsonPath("$.state").value("FAILED"));

        String newTag = jdbc.queryForObject("select id_tag from customer_id_tag where customer_id = ?", String.class,
                customer);
        assertThat(newTag).isNotEqualTo(oldTag);
        assertThat(jdbc.queryForObject("select id_tag from charging_session where customer_id = ? "
                + "order by created_at desc limit 1", String.class, customer)).isEqualTo(newTag);
        assertThat(OCPP.hits("POST", "/api/v1/charge-points/CP-5/connectors/1/start")).isEqualTo(2);
        OCPP.route("POST", "/api/v1/charge-points/CP-5/connectors/1/start", 200, "{\"status\":\"PENDING\"}");
    }

    @Test
    void theStopButtonEndsAChargeWhoseChargerIsUnreachableAndSilent() throws Exception {
        UUID customer = newCustomer();
        String sessionId = com.jayway.jsonpath.JsonPath.read(
                start(customer).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.id");
        platformShowsTheChargeRunning(jdbc.queryForObject(
                "select id_tag from customer_id_tag where customer_id = ?", String.class, customer));
        mvc.perform(get("/v1/sessions/active").with(TestTenants.customer(customer))).andExpect(status().isOk());
        jdbc.update("update charging_session set reading_at = now() - interval '5 minutes' where id = ?::uuid",
                sessionId);

        mvc.perform(post("/v1/sessions/" + sessionId + "/stop").with(TestTenants.customer(customer))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("ABANDONED"))
                .andExpect(jsonPath("$.stopReason").value("PowerLoss"));
    }
}
