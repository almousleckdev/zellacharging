package com.evcharge.customer.wallet;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WithdrawalsDisabledIntegrationTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    @Test
    void withdrawalsAreOffUntilAPayoutProviderIsVerified() throws Exception {
        mvc.perform(post("/v1/wallet/withdrawals").with(TestTenants.anyCustomer()).header("Idempotency-Key", "k")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amountMinor\":4000,\"operator\":\"AIRTEL\",\"password\":\"whatever123\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("WITHDRAWALS_UNAVAILABLE"));
    }
}
