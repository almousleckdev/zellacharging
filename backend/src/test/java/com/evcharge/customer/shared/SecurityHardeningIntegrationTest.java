package com.evcharge.customer.shared;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "zella.rate-limit.login-per-ip-per-minute=3",
        "zella.rate-limit.topup-per-customer-per-minute=2"})
class SecurityHardeningIntegrationTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    private static final String LOGIN = "{\"identifier\":\"nobody@example.com\",\"password\":\"whatever123\"}";

    @Test
    void repeatedLoginAttemptsFromOneAddressAreThrottled() throws Exception {
        for (int attempt = 0; attempt < 3; attempt++) {
            mvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.code").value("TOO_MANY_REQUESTS"))
                .andExpect(jsonPath("$.correlationId").isNotEmpty());
    }

    @Test
    void aTopUpWithoutAnIdempotencyKeyIsRejected() throws Exception {
        mvc.perform(post("/v1/wallet/topups").with(TestTenants.anyCustomer()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amountMinor\":5000,\"msisdn\":\"+24177123456\",\"operator\":\"AIRTEL\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REQUIRED"));
    }

    @Test
    void topUpAttemptsAreThrottledPerCustomerNotPerAddress() throws Exception {
        var busy = TestTenants.customer(java.util.UUID.randomUUID());
        for (int attempt = 0; attempt < 2; attempt++) {
            mvc.perform(post("/v1/wallet/topups").with(busy).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"amountMinor\":5000}"));
        }
        mvc.perform(post("/v1/wallet/topups").with(busy).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amountMinor\":5000}"))
                .andExpect(status().isTooManyRequests());
        mvc.perform(post("/v1/wallet/topups").with(TestTenants.anyCustomer()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amountMinor\":5000}"))
                .andExpect(status().isBadRequest());
    }
}
