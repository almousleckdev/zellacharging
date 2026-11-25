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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "zella.partner.feed-lag=0s")
@AutoConfigureMockMvc
class FreeSundayFeedTest extends PostgresTestBase {

    private static final AtomicInteger TRANSACTIONS = new AtomicInteger(700_000);
    private static final Instant BASE = Instant.parse("2026-11-22T10:00:00Z");

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PartnerKeys keys;

    @Autowired
    ObjectMapper mapper;

    private PartnerFixtures fixtures;
    private String key;
    private UUID driver;

    @BeforeEach
    void prepare() {
        fixtures = new PartnerFixtures(jdbc, keys);
        key = fixtures.keyFor("GA");
        driver = fixtures.customer("GA", "A", "B", UUID.randomUUID() + "@example.com", null, "ACTIVE");
        fixtures.link("GA", driver);
    }

    private UUID freeSession(String claimState) {
        UUID session = fixtures.session(driver, "COMPLETED", TRANSACTIONS.incrementAndGet(), BASE);
        hold(session);
        jdbc.update("insert into session_charge (session_id, customer_id, state, energy_cost_minor, vat_minor, "
                + "service_fee_minor, total_minor, currency, decimals, estimated, updated_at) "
                + "values (?, ?, 'CHARGED', 0, 0, 0, 0, 'XOF', 0, false, ?)", session, driver, Timestamp.from(BASE));
        if (claimState != null) {
            jdbc.update("insert into free_sunday_claim (id, customer_id, sunday_date, session_id, agreement_id, "
                    + "window_ends_at, state) values (?, ?, '2026-11-22', ?, ?, ?, ?)", UUID.randomUUID(), driver,
                    session, UUID.randomUUID(), Timestamp.from(BASE.plusSeconds(7200)), claimState);
        }
        return session;
    }

    private void hold(UUID session) {
        jdbc.update("insert into funding_hold (session_id, customer_id, reserved_minor, captured_minor, state, "
                + "created_at, waived) values (?, ?, 1000, 0, 'SETTLED', ?, true)", session, driver,
                Timestamp.from(BASE));
    }

    private JsonNode itemOf(UUID session) throws Exception {
        String body = mvc.perform(get("/v1/partners/ndzela/sessions").header("X-Api-Key", key))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        int transaction = jdbc.queryForObject("select transaction_id from charging_session where id = ?",
                Integer.class, session);
        for (JsonNode item : mapper.readTree(body).get("items")) {
            if (item.get("transactionId").asInt() == transaction) {
                return item;
            }
        }
        throw new AssertionError("session not in feed");
    }

    @Test
    void aClaimedFreeSessionIsMarkedFreeSundayWithAZeroCharge() throws Exception {
        JsonNode item = itemOf(freeSession("CLAIMED"));

        assertThat(item.get("freeCategory").asText()).isEqualTo("FREE_SUNDAY");
        assertThat(item.get("totalChargedMinor").asLong()).isZero();
    }

    @Test
    void aReleasedClaimIsNotMarkedFreeSunday() throws Exception {
        JsonNode item = itemOf(freeSession("RELEASED"));

        assertThat(item.get("freeCategory").isNull()).isTrue();
    }

    @Test
    void aComplimentaryStaffSessionStillShowsItsCategory() throws Exception {
        jdbc.update("insert into customer_complimentary (customer_id, category, granted_by, reason, "
                + "granted_at) values (?, 'NDZELA_STAFF', ?, 'staff', now())", driver, UUID.randomUUID());

        JsonNode item = itemOf(freeSession(null));

        assertThat(item.get("freeCategory").asText()).isEqualTo("NDZELA_STAFF");
    }

    @Test
    void aClaimedFreeSundayWinsOverTheStaffCategory() throws Exception {
        jdbc.update("insert into customer_complimentary (customer_id, category, granted_by, reason, "
                + "granted_at) values (?, 'NDZELA_STAFF', ?, 'staff', now())", driver, UUID.randomUUID());

        JsonNode item = itemOf(freeSession("CLAIMED"));

        assertThat(item.get("freeCategory").asText()).isEqualTo("FREE_SUNDAY");
    }
}
