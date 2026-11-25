package com.evcharge.customer.partner;

import com.evcharge.customer.PostgresTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "zella.partner.feed-lag=1h")
@AutoConfigureMockMvc
class SessionFeedLagTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PartnerKeys keys;

    @Test
    void changesAreHeldBackForTheLagEvenWhenTheirOwnTimesAreOld() throws Exception {
        PartnerFixtures fixtures = new PartnerFixtures(jdbc, keys);
        String key = fixtures.keyFor("GA");
        jdbc.update("delete from partner_link");
        UUID driver = fixtures.customer("GA", "A", "B", UUID.randomUUID() + "@example.com", null, "ACTIVE");
        jdbc.update("insert into partner_link (partner, customer_id, linked_at) values ('NDZELA', ?, now())", driver);
        fixtures.session(driver, "COMPLETED", 900_001, Instant.parse("2026-11-01T10:00:00Z"));

        mvc.perform(get("/v1/partners/ndzela/sessions").header("X-Api-Key", key))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items").isEmpty());
    }
}
