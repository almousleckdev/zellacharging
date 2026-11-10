package com.evcharge.customer.charging;

import com.evcharge.customer.FakeOcppServer;
import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import com.evcharge.customer.charging.application.SessionSweeper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrphanChargeIntegrationTest extends PostgresTestBase {

    private static final String STOP = "/api/v1/charge-points/CP-5/connectors/1/stop";

    private static final FakeOcppServer OCPP = new FakeOcppServer()
            .route("POST", "/api/v1/id-tags", 201, "{\"idTag\":\"x\"}")
            .route("POST", STOP, 200, "{\"status\":\"ACCEPTED\"}")
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
    SessionSweeper sweeper;

    @AfterEach
    void releaseTheConnector() {
        jdbc.update("update funding_hold set state = 'RELEASED' where state = 'OPEN' and session_id in "
                + "(select id from charging_session where id_tag like 'ORPHAN-%')");
        jdbc.update("update charging_session set state = 'COMPLETED' where id_tag like 'ORPHAN-%'");
    }

    private UUID customerWith(long wallet, String idTag) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, 'x', 'ACTIVE', now())", id, id + "@example.com");
        jdbc.update("insert into wallet (customer_id, currency, decimals, balance_minor, created_at) "
                + "values (?, 'XOF', 0, ?, now())", id, wallet);
        jdbc.update("insert into customer_id_tag (customer_id, id_tag, created_at) values (?, ?, now())", id, idTag);
        return id;
    }

    private void platformRunsAChargeFor(int transaction, String idTag) {
        OCPP.route("GET", "/api/v1/sessions/" + transaction + "/metering", 200,
                "{\"totalKwh\":1.0,\"currentPowerKw\":7.0,\"voltage\":230,"
                        + "\"sampledAt\":\"2026-11-10T09:00:00Z\"}");
        OCPP.route("GET", "/api/v1/sessions", 200, "{\"items\":[{\"transactionId\":" + transaction + ",\"deviceId\":\"CP-5\","
                + "\"connectorId\":1,\"status\":\"ACTIVE\",\"startedAt\":\"" + Instant.now().minusSeconds(90)
                + "\",\"idTag\":\"" + idTag + "\",\"pricePerKwhMinor\":150,\"currency\":\"XOF\","
                + "\"currencyDecimals\":0}]}");
    }

    @Test
    void aChargeRunningWithNoSessionIsAdoptedSoTheCustomerSeesItAndPaysForIt() throws Exception {
        String idTag = "ORPHAN-" + UUID.randomUUID();
        UUID customer = customerWith(100000, idTag);
        platformRunsAChargeFor(900, idTag);
        OCPP.resetHits();

        sweeper.sweep();

        mvc.perform(get("/v1/sessions/active").with(TestTenants.customer(customer)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.state").value("ACTIVE"));
        assertThat(jdbc.queryForObject("select transaction_id from charging_session where customer_id = ?",
                Integer.class, customer)).isEqualTo(900);
        assertThat(jdbc.queryForObject("select count(*) from funding_hold where customer_id = ? and state = 'OPEN'",
                Integer.class, customer)).isEqualTo(1);
        assertThat(OCPP.hits("POST", STOP)).isZero();
    }

    @Test
    void aChargeWhoseCustomerCannotPayIsStopped() throws Exception {
        String idTag = "BROKE-" + UUID.randomUUID();
        UUID customer = customerWith(0, idTag);
        platformRunsAChargeFor(901, idTag);
        OCPP.resetHits();

        sweeper.sweep();

        mvc.perform(get("/v1/sessions/active").with(TestTenants.customer(customer)))
                .andExpect(status().isNoContent());
        assertThat(OCPP.hits("POST", STOP)).isEqualTo(1);
    }
}
