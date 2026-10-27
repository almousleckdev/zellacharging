package com.evcharge.customer.charging;

import com.evcharge.customer.FakeOcppServer;
import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StartRulesIntegrationTest extends PostgresTestBase {

    private static final FakeOcppServer OCPP = new FakeOcppServer()
            .route("POST", "/api/v1/id-tags", 201, "{\"idTag\":\"x\"}")
            .route("GET", "/api/v1/sessions", 200, "{\"items\":[]}")
            .station("CP-OK")
            .station("CP-PLUGGED", "GA", true, "Preparing")
            .station("CP-BUSY", "GA", true, "Charging")
            .station("CP-FAULT", "GA", true, "Faulted")
            .station("CP-DOWN", "GA", false, "Available")
            .station("CP-MALI", "ML", true, "Available");

    static {
        for (String id : new String[]{"CP-OK", "CP-PLUGGED"}) {
            OCPP.route("POST", "/api/v1/charge-points/" + id + "/connectors/1/start", 200, "{\"status\":\"PENDING\"}");
        }
    }

    @DynamicPropertySource
    static void ocpp(DynamicPropertyRegistry registry) {
        registry.add("zella.ocpp.base-url", OCPP::baseUrl);
    }

    @AfterAll
    static void stop() {
        OCPP.stop();
    }

    private static final java.util.concurrent.atomic.AtomicInteger SEQUENCE = new java.util.concurrent.atomic.AtomicInteger();

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    private UUID customerWith(long balance) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, 'x', 'ACTIVE', now())", id, id + "@example.com");
        jdbc.update("insert into wallet (customer_id, currency, decimals, balance_minor, created_at) "
                + "values (?, 'XOF', 0, ?, now())", id, balance);
        return id;
    }

    private static String freshStation() {
        String id = "CP-N" + SEQUENCE.incrementAndGet();
        OCPP.station(id).route("POST", "/api/v1/charge-points/" + id + "/connectors/1/start", 200,
                "{\"status\":\"PENDING\"}");
        return id;
    }

    private static String body(String station, long amount) {
        return "{\"stationId\":\"" + station + "\",\"connectorId\":1,\"amountMinor\":" + amount + "}";
    }

    private ResultActions start(UUID customer, String station, long amount, String key) throws Exception {
        var request = post("/v1/sessions").with(TestTenants.customer(customer))
                .contentType(MediaType.APPLICATION_JSON).content(body(station, amount));
        return mvc.perform(key == null ? request : request.header("Idempotency-Key", key));
    }

    private long reserved(UUID customer) {
        return jdbc.queryForObject("select reserved_minor from wallet where customer_id = ?", Long.class, customer);
    }

    @Test
    void aFreeOrPluggedInConnectorCanStart() throws Exception {
        start(customerWith(10000), "CP-OK", 2000, null).andExpect(status().isCreated());
        start(customerWith(10000), "CP-PLUGGED", 2000, null).andExpect(status().isCreated());
    }

    @Test
    void aBusyFaultyOrOfflineConnectorNeverStartsAndNoFundsAreReserved() throws Exception {
        UUID customer = customerWith(10000);
        start(customer, "CP-BUSY", 2000, null).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONNECTOR_UNAVAILABLE"));
        start(customer, "CP-FAULT", 2000, null).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONNECTOR_UNAVAILABLE"));
        start(customer, "CP-DOWN", 2000, null).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STATION_OFFLINE"));
        assertThat(reserved(customer)).isZero();
    }

    @Test
    void aStationOfAnotherCountryIsInvisibleToThisCustomer() throws Exception {
        start(customerWith(10000), "CP-MALI", 2000, null).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("STATION_NOT_FOUND"));
    }

    @Test
    void anUnknownStationIsNotFound() throws Exception {
        start(customerWith(10000), "CP-NOWHERE", 2000, null).andExpect(status().isNotFound());
    }

    @Test
    void repeatingARequestWithTheSameKeyReturnsTheSameSessionAndReservesOnce() throws Exception {
        String station = freshStation();
        UUID customer = customerWith(10000);
        String first = start(customer, station, 2000, "key-1").andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String second = start(customer, station, 2000, "key-1").andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        assertThat(com.jayway.jsonpath.JsonPath.<String>read(second, "$.id"))
                .isEqualTo(com.jayway.jsonpath.JsonPath.<String>read(first, "$.id"));
        assertThat(reserved(customer)).isEqualTo(2000);
        assertThat(jdbc.queryForObject("select count(*) from charging_session where customer_id = ?",
                Integer.class, customer)).isOne();
    }

    @Test
    void reusingAKeyForADifferentAmountIsRejected() throws Exception {
        String station = freshStation();
        UUID customer = customerWith(10000);
        start(customer, station, 2000, "key-2").andExpect(status().isCreated());
        start(customer, station, 3000, "key-2").andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSED"));
    }

    @Test
    void amountsOutsideTheAllowedRangeAreRefusedByTheBackend() throws Exception {
        String station = freshStation();
        UUID customer = customerWith(1_000_000);
        start(customer, station, 10, null).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_AMOUNT"));
        start(customer, station, 900_000, null).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_AMOUNT"));
        assertThat(reserved(customer)).isZero();
    }

    @Test
    void theBackendQuoteReportsTheShortfallWithoutReservingAnything() throws Exception {
        String station = freshStation();
        UUID customer = customerWith(1500);
        mvc.perform(post("/v1/sessions/quote").with(TestTenants.customer(customer))
                        .contentType(MediaType.APPLICATION_JSON).content(body(station, 2000)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amountMinor").value(2000))
                .andExpect(jsonPath("$.availableMinor").value(1500))
                .andExpect(jsonPath("$.shortfallMinor").value(500))
                .andExpect(jsonPath("$.minimumMinor").value(1000))
                .andExpect(jsonPath("$.maximumMinor").value(200000));
        assertThat(reserved(customer)).isZero();
    }

    @Test
    void theQuoteAlsoRefusesAnUnavailableConnector() throws Exception {
        mvc.perform(post("/v1/sessions/quote").with(TestTenants.customer(customerWith(5000)))
                        .contentType(MediaType.APPLICATION_JSON).content(body("CP-BUSY", 2000)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("CONNECTOR_UNAVAILABLE"));
    }
}
