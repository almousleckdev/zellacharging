package com.evcharge.customer.partner;

import com.evcharge.customer.PostgresTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "zella.partner.feed-lag=0s")
@AutoConfigureMockMvc
class SessionFeedStampTest extends PostgresTestBase {

    private static final AtomicInteger TRANSACTIONS = new AtomicInteger(800_000);
    private static final Instant BASE = Instant.parse("2026-11-01T10:00:00Z");

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PartnerKeys keys;

    @Autowired
    ObjectMapper mapper;

    private String key;
    private UUID driver;
    private UUID session;
    private String cursor;

    @BeforeEach
    void readTheFeedUpToALiveCharge() throws Exception {
        PartnerFixtures fixtures = new PartnerFixtures(jdbc, keys);
        key = fixtures.keyFor("GA");
        jdbc.update("delete from partner_link");
        driver = fixtures.customer("GA", "A", "B", UUID.randomUUID() + "@example.com", null, "ACTIVE");
        jdbc.update("insert into partner_link (partner, customer_id, linked_at) values ('NDZELA', ?, now())", driver);
        session = fixtures.session(driver, "ACTIVE", TRANSACTIONS.incrementAndGet(), BASE);
        cursor = feed("").get("nextCursor").asText();
    }

    @Test
    void aChangeStampedWithAnOlderTimeStillReachesAReaderThatMovedPastIt() throws Exception {
        jdbc.update("update charging_session set state = 'COMPLETED', "
                + "updated_at = timestamptz '2026-10-01 00:00:00Z' where id = ?", session);

        assertThat(next().get(0).get("sessionState").asText()).isEqualTo("COMPLETED");
    }

    @Test
    void aMeterReadingIsSentEvenThoughItDoesNotTouchUpdatedAt() throws Exception {
        jdbc.update("update charging_session set energy_kwh = null, last_energy_kwh = 7.5 where id = ?", session);

        assertThat(next().get(0).get("energyKwh").decimalValue()).isEqualByComparingTo("7.5");
    }

    @Test
    void makingAChargeFreeSendsItAgain() throws Exception {
        jdbc.update("insert into funding_hold (session_id, customer_id, reserved_minor, state, waived, created_at) "
                + "values (?, ?, 1, 'OPEN', true, now())", session, driver);

        assertThat(next()).hasSize(1);
    }

    @Test
    void savingARowWithoutAnyChangeSendsNothing() throws Exception {
        jdbc.update("update charging_session set state = state where id = ?", session);

        assertThat(next()).isEmpty();
    }

    private JsonNode next() throws Exception {
        return feed("?cursor=" + cursor).get("items");
    }

    private JsonNode feed(String query) throws Exception {
        String body = mvc.perform(get("/v1/partners/ndzela/sessions" + query).header("X-Api-Key", key))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(body);
    }
}
