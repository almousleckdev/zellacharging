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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PublicSessionFeedIntegrationTest extends PostgresTestBase {

    private static final String PATH = "/v1/partners/ndzela/public-sessions";
    private static final AtomicInteger TRANSACTIONS = new AtomicInteger(700_000);
    private static final Instant BASE = Instant.parse("2026-11-01T10:00:00Z");

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
    private UUID publicCustomer;

    @BeforeEach
    void prepare() {
        fixtures = new PartnerFixtures(jdbc, keys);
        key = fixtures.keyFor("GA");
        jdbc.update("delete from partner_link");
        driver = fixtures.customer("GA", "Linked", "Driver", UUID.randomUUID() + "@example.com", null, "ACTIVE");
        jdbc.update("insert into partner_link (partner, customer_id, linked_at) values ('NDZELA', ?, now())", driver);
        publicCustomer = fixtures.customer("GA", "Public", "Person", UUID.randomUUID() + "@example.com", null, "ACTIVE");
    }

    private int session(UUID customer, int secondsAfterBase) {
        int transaction = TRANSACTIONS.incrementAndGet();
        fixtures.session(customer, "COMPLETED", transaction, BASE.plusSeconds(secondsAfterBase));
        return transaction;
    }

    private JsonNode feed(String query) throws Exception {
        String body = mvc.perform(get(PATH + query).header("X-Api-Key", key)).andExpect(status().isOk()).andReturn()
                .getResponse().getContentAsString();
        return mapper.readTree(body);
    }

    private static List<Integer> transactions(JsonNode page) {
        List<Integer> ids = new ArrayList<>();
        page.get("items").forEach(item -> ids.add(item.get("transactionId").asInt()));
        return ids;
    }

    @Test
    void theFeedHoldsTheSessionsOfCustomersNotLinkedToNdzela() throws Exception {
        int mine = session(driver, 1);
        int other = session(publicCustomer, 2);

        List<Integer> shown = transactions(feed(""));

        assertThat(shown).contains(other).doesNotContain(mine);
    }

    @Test
    void itCarriesNothingThatIdentifiesTheCustomer() throws Exception {
        int other = session(publicCustomer, 1);

        JsonNode item = null;
        for (JsonNode candidate : feed("").get("items")) {
            if (candidate.get("transactionId").asInt() == other) {
                item = candidate;
            }
        }

        assertThat(item).isNotNull();
        assertThat(item.has("customerId")).isFalse();
        assertThat(item.has("idTag")).isFalse();
        assertThat(item.toString()).doesNotContain("Public").doesNotContain("Person").doesNotContain("@example.com");
        assertThat(item.get("stationId").asText()).startsWith("CP-");
        assertThat(item.get("energyKwh").decimalValue()).isEqualByComparingTo("12.5");
    }

    @Test
    void aCustomerWhoBecomesLinkedLeavesThePublicFeed() throws Exception {
        int other = session(publicCustomer, 1);
        assertThat(transactions(feed(""))).contains(other);

        jdbc.update("insert into partner_link (partner, customer_id, linked_at) values ('NDZELA', ?, now())",
                publicCustomer);

        assertThat(transactions(feed(""))).doesNotContain(other);
    }

    @Test
    void theFeedPagesByCursorLikeTheLinkedOne() throws Exception {
        jdbc.update("delete from charging_session");
        int first = session(publicCustomer, 1);
        int second = session(publicCustomer, 2);
        int third = session(publicCustomer, 3);

        JsonNode page = feed("?limit=2");
        assertThat(transactions(page)).containsExactly(first, second);
        assertThat(page.get("hasMore").asBoolean()).isTrue();

        JsonNode next = feed("?limit=2&cursor=" + page.get("nextCursor").asText());
        assertThat(transactions(next)).containsExactly(third);
    }

    @Test
    void whenEveryoneIsLinkedThePublicFeedIsEmptyNotAnError() throws Exception {
        jdbc.update("delete from charging_session");
        jdbc.update("insert into partner_link (partner, customer_id, linked_at) values ('NDZELA', ?, now())",
                publicCustomer);
        session(driver, 1);
        session(publicCustomer, 2);

        assertThat(feed("").get("items")).isEmpty();
    }

    @Test
    void theFeedNeedsThePartnerKey() throws Exception {
        mvc.perform(get(PATH)).andExpect(status().isUnauthorized());
    }
}
