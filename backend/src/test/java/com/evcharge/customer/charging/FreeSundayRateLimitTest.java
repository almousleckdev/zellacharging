package com.evcharge.customer.charging;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "zella.rate-limit.free-sunday-per-customer-per-minute=2")
class FreeSundayRateLimitTest extends PostgresTestBase {

    private static final String PATH = "/v1/free-sunday";

    @Autowired
    MockMvc mvc;

    @Test
    void theCallAfterTheLimitIsRejectedPerCustomer() throws Exception {
        var busy = TestTenants.customer(UUID.randomUUID());
        for (int call = 0; call < 2; call++) {
            mvc.perform(get(PATH).with(busy)).andExpect(status().isOk());
        }

        mvc.perform(get(PATH).with(busy)).andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.code").value("TOO_MANY_REQUESTS"));
        mvc.perform(get(PATH).with(TestTenants.anyCustomer())).andExpect(status().isOk());
    }
}
