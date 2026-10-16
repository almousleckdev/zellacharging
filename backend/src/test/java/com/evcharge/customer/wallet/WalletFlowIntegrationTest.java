package com.evcharge.customer.wallet;

import com.evcharge.customer.FakeOcppServer;
import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.charging.application.SessionSweeper;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WalletFlowIntegrationTest extends PostgresTestBase {

    private static final FakeOcppServer OCPP = new FakeOcppServer()
            .route("POST", "/api/v1/id-tags", 201, "{\"idTag\":\"x\"}")
            .route("POST", "/api/v1/charge-points/CP-W/connectors/1/start", 200, "{\"status\":\"PENDING\"}")
            .route("POST", "/api/v1/charge-points/CP-W/connectors/1/stop", 200, "{\"status\":\"PENDING\"}")
            .route("GET", "/api/v1/sessions", 200, "{\"items\":[]}");

    private static final String START = "{\"stationId\":\"CP-W\",\"connectorId\":1,\"amountMinor\":2000}";
    private static final String TOPUP = "{\"amountMinor\":5000,\"msisdn\":\"+24177123456\",\"operator\":\"AIRTEL\"}";

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

    @Autowired
    SessionSweeper sweeper;

    private UUID newCustomer() {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, 'x', 'ACTIVE', now())", id, id + "@example.com");
        return id;
    }

    private ResultActions postAs(UUID customer, String path, String body, String key) throws Exception {
        var request = post(path).with(jwt().jwt(j -> j.subject(customer.toString())))
                .contentType(MediaType.APPLICATION_JSON).content(body);
        return mvc.perform(key == null ? request : request.header("Idempotency-Key", key));
    }

    private ResultActions getAs(UUID customer, String path) throws Exception {
        return mvc.perform(get(path).with(jwt().jwt(j -> j.subject(customer.toString()))));
    }

    @Test
    void emptyWalletBlocksStartWithPaymentRequired() throws Exception {
        UUID customer = newCustomer();
        getAs(customer, "/v1/wallet").andExpect(status().isOk()).andExpect(jsonPath("$.balanceMinor").value(0));
        postAs(customer, "/v1/sessions", START, null).andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_FUNDS"));
        assertThat(jdbc.queryForObject("select count(*) from charging_session where customer_id = ?",
                Integer.class, customer)).isZero();
    }

    @Test
    void topUpCreditsOnceEvenWhenRetriedWithTheSameKey() throws Exception {
        UUID customer = newCustomer();
        postAs(customer, "/v1/wallet/topups", TOPUP, "k-1").andExpect(status().isCreated())
                .andExpect(jsonPath("$.state").value("SUCCEEDED"));
        postAs(customer, "/v1/wallet/topups", TOPUP, "k-1").andExpect(status().isCreated());
        getAs(customer, "/v1/wallet").andExpect(jsonPath("$.balanceMinor").value(5000))
                .andExpect(jsonPath("$.availableMinor").value(5000));
        getAs(customer, "/v1/wallet/transactions").andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("TOPUP"));
    }

    @Test
    void invalidTopUpsAreRejected() throws Exception {
        UUID customer = newCustomer();
        postAs(customer, "/v1/wallet/topups",
                "{\"amountMinor\":5000,\"msisdn\":\"0612\",\"operator\":\"MOOV\"}", null)
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PHONE"));
        postAs(customer, "/v1/wallet/topups",
                "{\"amountMinor\":1,\"msisdn\":\"+24177123456\",\"operator\":\"MOOV\"}", null)
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_AMOUNT"));
    }

    @Test
    void completedSessionIsCapturedAndTheRestReleased() throws Exception {
        UUID customer = newCustomer();
        postAs(customer, "/v1/wallet/topups", TOPUP, null).andExpect(status().isCreated());
        String sessionId = com.jayway.jsonpath.JsonPath.read(
                postAs(customer, "/v1/sessions", START, null).andExpect(status().isCreated())
                        .andReturn().getResponse().getContentAsString(), "$.id");
        getAs(customer, "/v1/wallet").andExpect(jsonPath("$.reservedMinor").value(2000))
                .andExpect(jsonPath("$.availableMinor").value(3000));

        String idTag = jdbc.queryForObject("select id_tag from customer_id_tag where customer_id = ?",
                String.class, customer);
        OCPP.route("GET", "/api/v1/sessions", 200, "{\"items\":[{\"transactionId\":31,\"deviceId\":\"CP-W\","
                + "\"connectorId\":1,\"status\":\"COMPLETED\",\"startedAt\":\"" + Instant.now().minusSeconds(60)
                + "\",\"stoppedAt\":\"" + Instant.now() + "\",\"energyKwh\":2.0,\"costMinor\":300,"
                + "\"currency\":\"XOF\",\"currencyDecimals\":0,\"idTag\":\"" + idTag + "\"}]}");

        sweeper.sweep();

        getAs(customer, "/v1/wallet").andExpect(jsonPath("$.balanceMinor").value(4700))
                .andExpect(jsonPath("$.reservedMinor").value(0))
                .andExpect(jsonPath("$.availableMinor").value(4700));
        assertThat(jdbc.queryForObject("select state from funding_hold where session_id = ?::uuid", String.class,
                sessionId)).isEqualTo("SETTLED");
        sweeper.sweep();
        getAs(customer, "/v1/wallet").andExpect(jsonPath("$.balanceMinor").value(4700));
        OCPP.route("GET", "/api/v1/sessions", 200, "{\"items\":[]}");
    }

    @Test
    void amountAboveTheAvailableBalanceIsRefused() throws Exception {
        UUID customer = newCustomer();
        postAs(customer, "/v1/wallet/topups", TOPUP, null).andExpect(status().isCreated());
        postAs(customer, "/v1/sessions", "{\"stationId\":\"CP-W\",\"connectorId\":1,\"amountMinor\":5001}", null)
                .andExpect(status().isPaymentRequired()).andExpect(jsonPath("$.code").value("INSUFFICIENT_FUNDS"));
        postAs(customer, "/v1/sessions", "{\"stationId\":\"CP-W\",\"connectorId\":1,\"amountMinor\":10}", null)
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_AMOUNT"));
    }

    @Test
    void refusedStartReleasesTheHold() throws Exception {
        UUID customer = newCustomer();
        postAs(customer, "/v1/wallet/topups", TOPUP, null).andExpect(status().isCreated());
        OCPP.route("POST", "/api/v1/charge-points/CP-R/connectors/1/start", 409,
                "{\"status\":\"REJECTED\",\"reasonCode\":\"CHARGER_REFUSED\"}");
        postAs(customer, "/v1/sessions", "{\"stationId\":\"CP-R\",\"connectorId\":1,\"amountMinor\":2000}", null)
                .andExpect(jsonPath("$.state").value("FAILED"));
        getAs(customer, "/v1/wallet").andExpect(jsonPath("$.reservedMinor").value(0))
                .andExpect(jsonPath("$.availableMinor").value(5000));
    }

    @Test
    void walletsAreIsolatedBetweenCustomers() throws Exception {
        UUID rich = newCustomer();
        UUID other = newCustomer();
        postAs(rich, "/v1/wallet/topups", TOPUP, null).andExpect(status().isCreated());
        getAs(other, "/v1/wallet").andExpect(jsonPath("$.balanceMinor").value(0));
        String topupId = com.jayway.jsonpath.JsonPath.read(
                postAs(rich, "/v1/wallet/topups", TOPUP, "k-2").andReturn().getResponse().getContentAsString(),
                "$.id");
        getAs(other, "/v1/wallet/topups/" + topupId).andExpect(status().isNotFound());
    }
}
