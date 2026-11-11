package com.evcharge.customer.account;

import com.evcharge.customer.FakeOcppServer;
import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import com.evcharge.customer.account.application.port.PasswordHasher;
import com.evcharge.customer.partner.PartnerFixtures;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SuspendedAccountIntegrationTest extends PostgresTestBase {

    private static final FakeOcppServer OCPP = new FakeOcppServer()
            .route("POST", "/api/v1/id-tags", 201, "{\"idTag\":\"x\"}")
            .route("POST", "/api/v1/charge-points/CP-S/connectors/1/start", 200, "{\"status\":\"PENDING\"}")
            .route("GET", "/api/v1/sessions", 200, "{\"items\":[]}")
            .station("CP-S");

    private static final String START = "{\"stationId\":\"CP-S\",\"connectorId\":1,\"amountMinor\":2000}";
    private static final String TOPUP = "{\"amountMinor\":5000,\"msisdn\":\"+24177123456\",\"operator\":\"AIRTEL\"}";

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
    PasswordHasher hasher;

    private UUID suspendedCustomer(String email, long owedMinor) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, ?, 'ACTIVE', now())", id, email, hasher.hash("Secret123!"));
        jdbc.update("insert into wallet (customer_id, currency, decimals, balance_minor, created_at) "
                + "values (?, 'XOF', 0, 10000, now())", id);
        new PartnerFixtures(jdbc).suspend(TENANT, id, owedMinor);
        return id;
    }

    private ResultActions postAs(UUID customer, String path, String body) throws Exception {
        return mvc.perform(post(path).with(TestTenants.customer(customer)).contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", UUID.randomUUID().toString()).content(body));
    }

    @Test
    void anAccessTokenIssuedBeforeTheSuspensionCannotStartACharge() throws Exception {
        UUID customer = suspendedCustomer(UUID.randomUUID() + "@example.com", 69000);

        postAs(customer, "/v1/sessions", START).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_SUSPENDED"))
                .andExpect(jsonPath("$.owedMinor").value(69000));

        assertThat(jdbc.queryForObject("select count(*) from charging_session where customer_id = ?",
                Integer.class, customer)).isZero();
    }

    @Test
    void anAccessTokenIssuedBeforeTheSuspensionCannotTopUp() throws Exception {
        UUID customer = suspendedCustomer(UUID.randomUUID() + "@example.com", 69000);

        postAs(customer, "/v1/wallet/topups", TOPUP).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_SUSPENDED"));

        assertThat(jdbc.queryForObject("select count(*) from topup where customer_id = ?", Integer.class,
                customer)).isZero();
    }

    @Test
    void loggingInAsASuspendedCustomerShowsTheAmountOwed() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        suspendedCustomer(email, 92000);

        mvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"" + email + "\",\"password\":\"Secret123!\"}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("ACCOUNT_SUSPENDED"))
                .andExpect(jsonPath("$.owedMinor").value(92000));
    }

    @Test
    void aSuspendedCustomerCanStillReadTheirHistory() throws Exception {
        UUID customer = suspendedCustomer(UUID.randomUUID() + "@example.com", 69000);

        mvc.perform(get("/v1/sessions").with(TestTenants.customer(customer))).andExpect(status().isOk());
        mvc.perform(get("/v1/wallet").with(TestTenants.customer(customer))).andExpect(status().isOk());
    }
}
