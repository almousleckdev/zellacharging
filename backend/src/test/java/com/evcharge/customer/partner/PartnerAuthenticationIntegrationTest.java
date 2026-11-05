package com.evcharge.customer.partner;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "zella.partner.rate-per-minute=3")
class PartnerAuthenticationIntegrationTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    PartnerKeys keys;

    @Autowired
    JdbcTemplate jdbc;

    private String key(String country) {
        return new PartnerFixtures(jdbc, keys).keyFor(country);
    }

    @Test
    void missingKeyIs401WithCodePartnerKeyInvalid() throws Exception {
        mvc.perform(get("/v1/partners/ping")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("PARTNER_KEY_INVALID"));
    }

    @Test
    void wrongKeyIs401() throws Exception {
        mvc.perform(get("/v1/partners/ping").header("X-Api-Key", "zpk_wrong")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("PARTNER_KEY_INVALID"));
    }

    @Test
    void revokedKeyIs401() throws Exception {
        IssuedKey issued = keys.issue("NDZELA", "GA", UUID.randomUUID());
        keys.revoke(issued.id(), UUID.randomUUID());

        mvc.perform(get("/v1/partners/ping").header("X-Api-Key", issued.key())).andExpect(status().isUnauthorized());
    }

    @Test
    void customerJwtIsRejectedOnPartnerPaths() throws Exception {
        mvc.perform(get("/v1/partners/ping").with(TestTenants.anyCustomer())).andExpect(status().isUnauthorized());
    }

    @Test
    void partnerKeyIsRejectedOnCustomerPaths() throws Exception {
        mvc.perform(get("/v1/wallet").header("X-Api-Key", key("GA"))).andExpect(status().isUnauthorized());
    }

    @Test
    void validKeyBindsItsCountry() throws Exception {
        jdbc.update("update country set active = true where code = 'ML'");
        mvc.perform(get("/v1/partners/ping").header("X-Api-Key", key("GA"))).andExpect(status().isOk())
                .andExpect(content().string("GA"));
        mvc.perform(get("/v1/partners/ping").header("X-Api-Key", key("ML"))).andExpect(status().isOk())
                .andExpect(content().string("ML"));
        jdbc.update("update country set active = false where code = 'ML'");
    }

    @Test
    void keyForAnInactiveCountryIsRejected() throws Exception {
        jdbc.update("update country set active = false where code = 'ML'");
        mvc.perform(get("/v1/partners/ping").header("X-Api-Key", key("ML"))).andExpect(status().isUnauthorized());
    }

    @Test
    void rateLimitReturns429WithRetryAfterAndEachKeyHasItsOwnBucket() throws Exception {
        String first = key("GA");
        String second = key("GA");
        for (int i = 0; i < 3; i++) {
            mvc.perform(get("/v1/partners/ping").header("X-Api-Key", first)).andExpect(status().isOk());
        }

        mvc.perform(get("/v1/partners/ping").header("X-Api-Key", first)).andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After")).andExpect(jsonPath("$.code").value("TOO_MANY_REQUESTS"));
        mvc.perform(get("/v1/partners/ping").header("X-Api-Key", second)).andExpect(status().isOk());
    }

    @Test
    void responsesCarryTheSecurityHeaders() throws Exception {
        mvc.perform(get("/v1/partners/ping").header("X-Api-Key", key("GA")))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().exists("Content-Security-Policy"));
    }
}
