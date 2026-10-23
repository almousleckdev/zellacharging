package com.evcharge.customer.charging;

import com.evcharge.customer.FakeOcppServer;
import com.evcharge.customer.PostgresTestBase;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ChargingFlowIntegrationTest extends PostgresTestBase {

    private static final FakeOcppServer OCPP = new FakeOcppServer()
            .route("POST", "/api/v1/id-tags", 201, "{\"idTag\":\"x\"}")
            .route("POST", "/api/v1/charge-points/CP-1/connectors/1/start", 200, "{\"status\":\"PENDING\"}")
            .route("POST", "/api/v1/charge-points/CP-1/connectors/1/stop", 200, "{\"status\":\"PENDING\"}")
            .route("POST", "/api/v1/charge-points/CP-7/connectors/1/start", 200, "{\"status\":\"PENDING\"}")
            .route("GET", "/api/v1/sessions", 200, "{\"items\":[]}");

    @DynamicPropertySource
    static void ocpp(DynamicPropertyRegistry registry) {
        registry.add("ndzela.ocpp.base-url", OCPP::baseUrl);
    }

    @AfterAll
    static void stop() {
        OCPP.stop();
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    private UUID newCustomer() {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, 'x', 'ACTIVE', now())", id, id + "@example.com");
        jdbc.update("insert into wallet (customer_id, currency, decimals, balance_minor, created_at) "
                + "values (?, 'XOF', 0, 100000, now())", id);
        return id;
    }

    private ResultActions postAs(UUID customer, String path, String body) throws Exception {
        return mvc.perform(post(path).with(jwt().jwt(j -> j.subject(customer.toString())))
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions getAs(UUID customer, String path) throws Exception {
        return mvc.perform(get(path).with(jwt().jwt(j -> j.subject(customer.toString()))));
    }

    @Test
    void startReconcileAndStopLifecycle() throws Exception {
        UUID customer = newCustomer();
        String start = "{\"stationId\":\"CP-1\",\"connectorId\":1,\"amountMinor\":2000}";

        String sessionId = com.jayway.jsonpath.JsonPath.read(
                postAs(customer, "/v1/sessions", start).andExpect(status().isCreated())
                        .andExpect(jsonPath("$.state").value("START_ACCEPTED"))
                        .andReturn().getResponse().getContentAsString(), "$.id");

        postAs(customer, "/v1/sessions", start).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONNECTOR_OR_SESSION_BUSY"));

        String idTag = jdbc.queryForObject("select id_tag from customer_id_tag where customer_id = ?",
                String.class, customer);
        String running = "{\"items\":[{\"transactionId\":7,\"deviceId\":\"CP-1\",\"connectorId\":1,"
                + "\"status\":\"ACTIVE\",\"startedAt\":\"" + Instant.now() + "\",\"idTag\":\"" + idTag + "\"}]}";
        OCPP.route("GET", "/api/v1/sessions", 200, running)
                .route("GET", "/api/v1/sessions/7/metering", 200,
                        "{\"totalKwh\":1.5,\"currentPowerKw\":30.0,\"stateOfChargePercent\":40,\"durationSeconds\":90,\"voltage\":402.1,\"currentAmpere\":74.6}");

        getAs(customer, "/v1/sessions/active").andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("ACTIVE"))
                .andExpect(jsonPath("$.metering.totalKwh").value(1.5))
                .andExpect(jsonPath("$.metering.voltage").value(402.1))
                .andExpect(jsonPath("$.metering.currentAmpere").value(74.6))
                .andExpect(jsonPath("$.batteryPercent").value(40));

        postAs(customer, "/v1/sessions/" + sessionId + "/stop", "{}").andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("STOPPING"));

        String done = "{\"items\":[{\"transactionId\":7,\"deviceId\":\"CP-1\",\"connectorId\":1,"
                + "\"status\":\"COMPLETED\",\"startedAt\":\"" + Instant.now().minusSeconds(60) + "\","
                + "\"stoppedAt\":\"" + Instant.now() + "\",\"energyKwh\":2.0,\"costMinor\":300,"
                + "\"currency\":\"XOF\",\"currencyDecimals\":0,\"stopReason\":\"Remote\"}]}";
        OCPP.route("GET", "/api/v1/sessions", 200, done);

        getAs(customer, "/v1/sessions/" + sessionId).andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("COMPLETED"))
                .andExpect(jsonPath("$.costMinor").value(300))
                .andExpect(jsonPath("$.currency").value("XOF"))
                .andExpect(jsonPath("$.batteryPercent").value(40))
                .andExpect(jsonPath("$.fees.electricMinor").value(300))
                .andExpect(jsonPath("$.fees.vatMinor").value(0))
                .andExpect(jsonPath("$.fees.totalMinor").value(300));
        getAs(customer, "/v1/sessions/active").andExpect(status().isNoContent());
    }

    @Test
    void chargerRefusalMarksSessionFailedAndFreesTheConnector() throws Exception {
        UUID customer = newCustomer();
        OCPP.route("POST", "/api/v1/charge-points/CP-9/connectors/1/start", 409,
                "{\"status\":\"REJECTED\",\"reasonCode\":\"TARIFF_NOT_CONFIGURED\"}");
        postAs(customer, "/v1/sessions", "{\"stationId\":\"CP-9\",\"connectorId\":1,\"amountMinor\":2000}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.state").value("FAILED"))
                .andExpect(jsonPath("$.failureCode").value("TARIFF_NOT_CONFIGURED"));
        getAs(customer, "/v1/sessions/active").andExpect(status().isNoContent());
    }

    @Test
    void otherCustomerCannotSeeASession() throws Exception {
        UUID owner = newCustomer();
        UUID stranger = newCustomer();
        OCPP.route("POST", "/api/v1/charge-points/CP-2/connectors/1/start", 200, "{\"status\":\"PENDING\"}");
        String id = com.jayway.jsonpath.JsonPath.read(
                postAs(owner, "/v1/sessions", "{\"stationId\":\"CP-2\",\"connectorId\":1,\"amountMinor\":2000}")
                        .andReturn().getResponse().getContentAsString(), "$.id");
        getAs(stranger, "/v1/sessions/" + id).andExpect(status().isNotFound());
    }

    private UUID finishedSession(UUID customer, String station) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into charging_session (id, customer_id, station_id, connector_id, id_tag, state, "
                + "created_at, updated_at) values (?, ?, ?, 1, 'x', 'COMPLETED', now(), now())", id, customer, station);
        return id;
    }

    private ResultActions deleteAs(UUID customer, String path) throws Exception {
        return mvc.perform(delete(path).with(jwt().jwt(j -> j.subject(customer.toString()))));
    }

    @Test
    void deletingAFinishedSessionRemovesItFromTheHistory() throws Exception {
        UUID customer = newCustomer();
        UUID id = finishedSession(customer, "CP-5");

        getAs(customer, "/v1/sessions").andExpect(jsonPath("$[0].id").value(id.toString()));
        deleteAs(customer, "/v1/sessions/" + id).andExpect(status().isNoContent());

        getAs(customer, "/v1/sessions").andExpect(jsonPath("$").isEmpty());
        getAs(customer, "/v1/sessions/" + id).andExpect(status().isNotFound());
    }

    @Test
    void aSessionInProgressCannotBeDeleted() throws Exception {
        UUID customer = newCustomer();
        String start = "{\"stationId\":\"CP-7\",\"connectorId\":1,\"amountMinor\":2000}";
        String id = com.jayway.jsonpath.JsonPath.read(postAs(customer, "/v1/sessions", start)
                .andReturn().getResponse().getContentAsString(), "$.id");

        deleteAs(customer, "/v1/sessions/" + id).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSION_STILL_ACTIVE"));
    }

    @Test
    void aCustomerCannotDeleteSomeoneElsesSession() throws Exception {
        UUID owner = newCustomer();
        UUID stranger = newCustomer();
        UUID id = finishedSession(owner, "CP-6");

        deleteAs(stranger, "/v1/sessions/" + id).andExpect(status().isNotFound());
        getAs(owner, "/v1/sessions/" + id).andExpect(status().isOk());
    }
}
