package com.evcharge.customer.charging;

import com.evcharge.customer.FakeOcppServer;
import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import com.jayway.jsonpath.JsonPath;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CancelWaitingIntegrationTest extends PostgresTestBase {

    private static final long WALLET = 100000;
    private static final String START = "{\"stationId\":\"CP-6\",\"connectorId\":1,\"amountMinor\":5000}";

    private static final FakeOcppServer OCPP = new FakeOcppServer()
            .route("POST", "/api/v1/id-tags", 201, "{\"idTag\":\"x\"}")
            .route("POST", "/api/v1/charge-points/CP-6/connectors/1/start", 200, "{\"status\":\"PENDING\"}")
            .route("POST", "/api/v1/charge-points/CP-6/connectors/1/stop", 200, "{\"status\":\"ACCEPTED\"}")
            .route("GET", "/api/v1/sessions", 200, "{\"items\":[],\"page\":0,\"size\":100,\"total\":0}")
            .station("CP-6");

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

    private UUID newCustomer() {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, 'x', 'ACTIVE', now())", id, id + "@example.com");
        jdbc.update("insert into wallet (customer_id, currency, decimals, balance_minor, created_at) "
                + "values (?, 'XOF', 0, ?, now())", id, WALLET);
        return id;
    }

    @Test
    void aDriverWhoDoesNotPlugInCanCancelAndKeepsTheirMoney() throws Exception {
        UUID customer = newCustomer();
        String body = mvc.perform(post("/v1/sessions").with(TestTenants.customer(customer))
                        .contentType(MediaType.APPLICATION_JSON).content(START))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.state").value("START_ACCEPTED")).andExpect(jsonPath("$.waitingUntil").exists())
                .andReturn().getResponse().getContentAsString();
        String sessionId = JsonPath.read(body, "$.id");

        mvc.perform(post("/v1/sessions/" + sessionId + "/stop").with(TestTenants.customer(customer)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.state").value("FAILED"))
                .andExpect(jsonPath("$.failureCode").value("CANCELLED"));

        mvc.perform(get("/v1/sessions/active").with(TestTenants.customer(customer)))
                .andExpect(status().isNoContent());
        Long balance = jdbc.queryForObject("select balance_minor from wallet where customer_id = ?", Long.class,
                customer);
        assertThat(balance).isEqualTo(WALLET);
    }
}
