package com.evcharge.customer.shared;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiProtectionIntegrationTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @ParameterizedTest
    @ValueSource(strings = {"/v1/wallet", "/v1/wallet/transactions", "/v1/wallet/withdrawals", "/v1/sessions",
            "/v1/sessions/active", "/v1/profile", "/v1/admin/overview"})
    void sensitiveEndpointsRefuseAnonymousCallers(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isUnauthorized());
    }

    @Test
    void everyResponseCarriesTheSecurityHeaders() throws Exception {
        mvc.perform(get("/v1/wallet").with(TestTenants.anyCustomer()))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'"))
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")));
    }

    @Test
    void anOversizedBodyIsRefusedBeforeItIsRead() throws Exception {
        String huge = "{\"padding\":\"" + "a".repeat(2_100_000) + "\"}";

        mvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(huge))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.code").value("PAYLOAD_TOO_LARGE"));
    }

    @Test
    void aCustomerCannotReadAnotherCustomersWithdrawalOrTopupOrSession() throws Exception {
        UUID stranger = UUID.randomUUID();
        for (String path : new String[] {"/v1/wallet/withdrawals/", "/v1/wallet/topups/", "/v1/sessions/",
                "/v1/wallet/transactions/"}) {
            mvc.perform(get(path + UUID.randomUUID()).with(TestTenants.customer(stranger)))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void theHealthEndpointIsPublicButRevealsNothingElse() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
        mvc.perform(get("/actuator/beans")).andExpect(status().isUnauthorized());
    }
}
