package com.evcharge.customer.charging;

import com.evcharge.customer.FakeOcppServer;
import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"zella.charging.user-refresh-interval=60s", "zella.charging.metering-interval=60s"})
class OcppQuotaIntegrationTest extends PostgresTestBase {

    private static final int SESSIONS = 6;
    private static final String LIST = "/api/v1/sessions";

    private static final FakeOcppServer OCPP = new FakeOcppServer()
            .route("POST", "/api/v1/id-tags", 201, "{\"idTag\":\"x\"}")
            .route("GET", LIST, 200, "{\"items\":[]}");

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

    private record Live(UUID customer, UUID session, String idTag, String station, int transaction) {
    }

    private Live start(int index) throws Exception {
        String station = "CP-Q" + index + "-" + UUID.randomUUID().toString().substring(0, 4);
        OCPP.station(station).route("POST", "/api/v1/charge-points/" + station + "/connectors/1/start", 200,
                "{\"status\":\"PENDING\"}");
        UUID customer = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, 'x', 'ACTIVE', now())", customer, customer + "@example.com");
        jdbc.update("insert into wallet (customer_id, currency, decimals, balance_minor, created_at) "
                + "values (?, 'XOF', 0, 100000, now())", customer);
        String body = mvc.perform(post("/v1/sessions").with(TestTenants.customer(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stationId\":\"" + station + "\",\"connectorId\":1,\"amountMinor\":20000}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        UUID session = UUID.fromString(com.jayway.jsonpath.JsonPath.read(body, "$.id"));
        String idTag = jdbc.queryForObject("select id_tag from customer_id_tag where customer_id = ?", String.class,
                customer);
        return new Live(customer, session, idTag, station, 9000 + index);
    }

    private static String activeItem(Live live) {
        return "{\"transactionId\":" + live.transaction() + ",\"deviceId\":\"" + live.station()
                + "\",\"connectorId\":1,\"status\":\"ACTIVE\",\"startedAt\":\"" + Instant.now()
                + "\",\"idTag\":\"" + live.idTag() + "\",\"pricePerKwhMinor\":150}";
    }

    @Test
    void manyLiveSessionsCostOneListCallAndOneMeteringCallEachPerSweep() throws Exception {
        jdbc.update("update funding_hold set state = 'RELEASED', closed_at = now() where state = 'OPEN'");
        List<Live> lives = new ArrayList<>();
        for (int index = 0; index < SESSIONS; index++) {
            lives.add(start(index));
        }
        OCPP.route("GET", LIST, 200, "{\"items\":[" + String.join(",", lives.stream()
                .map(OcppQuotaIntegrationTest::activeItem).toList()) + "]}");
        for (Live live : lives) {
            OCPP.route("GET", LIST + "/" + live.transaction() + "/metering", 200,
                    "{\"totalKwh\":1.0,\"currentPowerKw\":20.0,\"sampledAt\":\"" + Instant.now() + "\"}");
        }
        OCPP.resetHits();

        sweeper.sweep();

        assertThat(OCPP.hits("GET", LIST)).isEqualTo(1);
        for (Live live : lives) {
            assertThat(OCPP.hits("GET", LIST + "/" + live.transaction() + "/metering")).isEqualTo(1);
        }
        for (Live live : lives) {
            mvc.perform(get("/v1/sessions/" + live.session()).with(TestTenants.customer(live.customer())))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.state").value("ACTIVE"));
        }
        assertThat(OCPP.hits("GET", LIST)).isEqualTo(1);
        for (Live live : lives) {
            assertThat(OCPP.hits("GET", LIST + "/" + live.transaction() + "/metering")).isEqualTo(1);
        }
    }
}
