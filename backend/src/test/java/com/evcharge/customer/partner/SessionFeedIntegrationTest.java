package com.evcharge.customer.partner;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "zella.partner.feed-lag=0s")
@AutoConfigureMockMvc
class SessionFeedIntegrationTest extends PostgresTestBase {

    private static final String PATH = "/v1/partners/ndzela/sessions";
    private static final AtomicInteger TRANSACTIONS = new AtomicInteger(500_000);
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

    @BeforeEach
    void prepare() {
        fixtures = new PartnerFixtures(jdbc, keys);
        key = fixtures.keyFor("GA");
        jdbc.update("delete from partner_link");
        driver = linkedCustomer();
    }

    private UUID linkedCustomer() {
        UUID customer = fixtures.customer("GA", "A", "B", UUID.randomUUID() + "@example.com", null, "ACTIVE");
        link(customer);
        return customer;
    }

    private void link(UUID customer) {
        jdbc.update("insert into partner_link (partner, customer_id, linked_at) values ('NDZELA', ?, now())",
                customer);
    }

    private UUID session(UUID customer, String state, Integer transactionId, int secondsAfterBase) {
        return fixtures.session(customer, state, transactionId, BASE.plusSeconds(secondsAfterBase));
    }

    private UUID finished(UUID customer, int secondsAfterBase) {
        return session(customer, "COMPLETED", TRANSACTIONS.incrementAndGet(), secondsAfterBase);
    }

    private void charge(UUID sessionId, UUID customer, String state, Long total, boolean estimated, int seconds) {
        jdbc.update("insert into session_charge (session_id, customer_id, state, energy_cost_minor, vat_minor, "
                + "service_fee_minor, total_minor, currency, decimals, estimated, updated_at) "
                + "values (?, ?, ?, ?, ?, ?, ?, 'XOF', 0, ?, ?)", sessionId, customer, state,
                total == null ? null : 1000L, total == null ? null : 180L, total == null ? null : 50L, total,
                estimated, Timestamp.from(BASE.plusSeconds(seconds)));
    }

    private ResultActions call(String query) throws Exception {
        return mvc.perform(get(PATH + query).header("X-Api-Key", key));
    }

    private JsonNode feed(String query) throws Exception {
        String body = call(query).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(body);
    }

    private static List<Integer> transactions(JsonNode page) {
        List<Integer> ids = new ArrayList<>();
        page.get("items").forEach(item -> ids.add(item.get("transactionId").asInt()));
        return ids;
    }

    @Test
    void firstCallWithoutCursorReadsFromTheBeginning() throws Exception {
        finished(driver, 1);
        finished(driver, 2);
        finished(driver, 3);

        JsonNode page = feed("");

        assertThat(page.get("items")).hasSize(3);
        assertThat(page.get("hasMore").asBoolean()).isFalse();
        assertThat(page.get("nextCursor").asText()).isNotBlank();
    }

    @Test
    void cursorContinuesWithoutRepeatOrSkip() throws Exception {
        for (int i = 1; i <= 5; i++) {
            finished(driver, i);
        }
        Set<Integer> seen = new HashSet<>();

        JsonNode first = feed("?limit=2");
        JsonNode second = feed("?limit=2&cursor=" + first.get("nextCursor").asText());
        JsonNode third = feed("?limit=2&cursor=" + second.get("nextCursor").asText());

        assertThat(first.get("hasMore").asBoolean()).isTrue();
        assertThat(second.get("hasMore").asBoolean()).isTrue();
        assertThat(third.get("hasMore").asBoolean()).isFalse();
        for (JsonNode page : List.of(first, second, third)) {
            seen.addAll(transactions(page));
        }
        assertThat(seen).hasSize(5);
        assertThat(transactions(first).size() + transactions(second).size() + transactions(third).size())
                .isEqualTo(5);
    }

    @Test
    void equalTimestampsNeitherRepeatNorSkip() throws Exception {
        for (int i = 0; i < 4; i++) {
            finished(driver, 10);
        }
        List<Integer> all = new ArrayList<>();
        String cursor = null;

        for (int page = 0; page < 6; page++) {
            JsonNode read = feed("?limit=1" + (cursor == null ? "" : "&cursor=" + cursor));
            all.addAll(transactions(read));
            cursor = read.get("nextCursor").asText();
        }

        assertThat(all).hasSize(4).doesNotHaveDuplicates();
    }

    @Test
    void unlinkedCustomerNeverAppearsAndUnlinkStopsNewItems() throws Exception {
        UUID stranger = fixtures.customer("GA", "S", "T", UUID.randomUUID() + "@example.com", null, "ACTIVE");
        finished(stranger, 1);
        finished(driver, 2);
        assertThat(feed("").get("items")).hasSize(1);

        jdbc.update("delete from partner_link where customer_id = ?", driver);
        finished(driver, 3);

        assertThat(feed("").get("items")).isEmpty();
    }

    @Test
    void sessionWithoutTransactionIdIsExcluded() throws Exception {
        session(driver, "FAILED", null, 1);
        finished(driver, 2);

        assertThat(feed("").get("items")).hasSize(1);
    }

    @Test
    void liveSessionIsPendingWithoutAmounts() throws Exception {
        session(driver, "ACTIVE", TRANSACTIONS.incrementAndGet(), 1);

        JsonNode item = feed("").get("items").get(0);

        assertThat(item.get("paymentState").asText()).isEqualTo("PENDING");
        assertThat(item.get("sessionState").asText()).isEqualTo("ACTIVE");
        assertThat(item.get("totalChargedMinor").isNull()).isTrue();
    }

    @Test
    void liveSessionCarriesItsBatteryPowerPriceAndLiveEnergy() throws Exception {
        UUID id = session(driver, "ACTIVE", TRANSACTIONS.incrementAndGet(), 1);
        jdbc.update("update charging_session set energy_kwh = null, last_energy_kwh = 4.2, start_battery_percent = 40, "
                + "battery_percent = 93, last_power_kw = 30.5, price_per_kwh_minor = 350 where id = ?", id);

        JsonNode item = feed("").get("items").get(0);

        assertThat(item.get("energyKwh").decimalValue()).isEqualByComparingTo("4.2");
        assertThat(item.get("batteryStartPercent").decimalValue()).isEqualByComparingTo("40");
        assertThat(item.get("batteryPercent").decimalValue()).isEqualByComparingTo("93");
        assertThat(item.get("powerKw").decimalValue()).isEqualByComparingTo("30.5");
        assertThat(item.get("pricePerKwhMinor").asLong()).isEqualTo(350L);
    }

    @Test
    void sessionWithoutReadingsReportsNullsNotZeros() throws Exception {
        UUID id = session(driver, "ACTIVE", TRANSACTIONS.incrementAndGet(), 1);
        jdbc.update("update charging_session set energy_kwh = null where id = ?", id);

        JsonNode item = feed("").get("items").get(0);

        assertThat(item.get("energyKwh").isNull()).isTrue();
        assertThat(item.get("batteryPercent").isNull()).isTrue();
        assertThat(item.get("powerKw").isNull()).isTrue();
    }

    @Test
    void settledSessionShowsTheBreakdownAndWalletMethod() throws Exception {
        UUID id = finished(driver, 1);
        charge(id, driver, "CHARGED", 1230L, false, 2);

        JsonNode item = feed("").get("items").get(0);

        assertThat(item.get("paymentState").asText()).isEqualTo("CHARGED");
        assertThat(item.get("paymentMethod").asText()).isEqualTo("WALLET");
        assertThat(item.get("energyCostMinor").asLong()).isEqualTo(1000);
        assertThat(item.get("vatMinor").asLong()).isEqualTo(180);
        assertThat(item.get("serviceFeeMinor").asLong()).isEqualTo(50);
        assertThat(item.get("totalChargedMinor").asLong()).isEqualTo(1230);
        assertThat(item.get("currency").asText()).isEqualTo("XOF");
        assertThat(item.get("currencyDecimals").asInt()).isZero();
        assertThat(item.get("breakdownEstimated").asBoolean()).isFalse();
        assertThat(item.get("customerId").asText()).isEqualTo(driver.toString());
        assertThat(item.get("idTag").asText()).isEqualTo("Z-TAG");
        assertThat(item.get("energyKwh").asDouble()).isEqualTo(12.5);
    }

    @Test
    void failedStartThatReachedTheChargerIsNotCharged() throws Exception {
        UUID id = session(driver, "FAILED", TRANSACTIONS.incrementAndGet(), 1);
        charge(id, driver, "NOT_CHARGED", null, false, 2);

        JsonNode item = feed("").get("items").get(0);

        assertThat(item.get("paymentState").asText()).isEqualTo("NOT_CHARGED");
        assertThat(item.get("totalChargedMinor").isNull()).isTrue();
    }

    @Test
    void underReviewRefundedAndEstimatedStatesAreReported() throws Exception {
        UUID review = finished(driver, 1);
        charge(review, driver, "UNDER_REVIEW", null, false, 2);
        UUID refunded = finished(driver, 3);
        charge(refunded, driver, "REFUNDED", 1230L, false, 4);
        UUID old = finished(driver, 5);
        charge(old, driver, "CHARGED", 1230L, true, 6);

        JsonNode items = feed("").get("items");

        assertThat(items.get(0).get("paymentState").asText()).isEqualTo("UNDER_REVIEW");
        assertThat(items.get(1).get("paymentState").asText()).isEqualTo("REFUNDED");
        assertThat(items.get(2).get("breakdownEstimated").asBoolean()).isTrue();
    }

    @Test
    void lateSettlementReappearsWithANewerUpdatedAtAndTheSameTransaction() throws Exception {
        UUID id = session(driver, "ACTIVE", TRANSACTIONS.incrementAndGet(), 1);
        JsonNode first = feed("");
        int transaction = first.get("items").get(0).get("transactionId").asInt();

        jdbc.update("update charging_session set state = 'COMPLETED', updated_at = ? where id = ?",
                Timestamp.from(BASE.plusSeconds(60)), id);
        charge(id, driver, "CHARGED", 1230L, false, 61);
        JsonNode second = feed("?cursor=" + first.get("nextCursor").asText());

        assertThat(second.get("items")).hasSize(1);
        JsonNode item = second.get("items").get(0);
        assertThat(item.get("transactionId").asInt()).isEqualTo(transaction);
        assertThat(item.get("paymentState").asText()).isEqualTo("CHARGED");
        assertThat(Instant.parse(item.get("updatedAt").asText()))
                .isAfter(Instant.parse(first.get("items").get(0).get("updatedAt").asText()));
    }

    @Test
    void aDriverLinkedLaterCanReadTheirHistoryEvenWhenTheGlobalCursorMovedOn() throws Exception {
        UUID late = fixtures.customer("GA", "L", "T", UUID.randomUUID() + "@example.com", null, "ACTIVE");
        finished(late, 1);
        finished(late, 2);
        finished(driver, 100);
        String cursor = feed("").get("nextCursor").asText();
        link(late);

        assertThat(feed("?cursor=" + cursor).get("items")).isEmpty();
        JsonNode history = feed("?customerId=" + late);

        assertThat(history.get("items")).hasSize(2);
        history.get("items").forEach(item -> assertThat(item.get("customerId").asText()).isEqualTo(late.toString()));
        JsonNode firstPage = feed("?customerId=" + late + "&limit=1");
        assertThat(firstPage.get("hasMore").asBoolean()).isTrue();
        assertThat(feed("?customerId=" + late + "&limit=1&cursor=" + firstPage.get("nextCursor").asText())
                .get("items")).hasSize(1);
    }

    @Test
    void historyOfACustomerWhoIsNotLinkedIs404() throws Exception {
        UUID stranger = fixtures.customer("GA", "S", "T", UUID.randomUUID() + "@example.com", null, "ACTIVE");
        finished(stranger, 1);

        call("?customerId=" + stranger).andExpect(status().isNotFound());
        call("?customerId=" + UUID.randomUUID()).andExpect(status().isNotFound());
    }

    @Test
    void emptyPageWithACursorReturnsTheSameCursorAndNoMore() throws Exception {
        finished(driver, 1);
        JsonNode first = feed("");
        String cursor = first.get("nextCursor").asText();

        JsonNode again = feed("?cursor=" + cursor);

        assertThat(again.get("items")).isEmpty();
        assertThat(again.get("nextCursor").asText()).isEqualTo(cursor);
        assertThat(again.get("hasMore").asBoolean()).isFalse();
    }

    @Test
    void emptyFirstCallHasNoCursor() throws Exception {
        JsonNode page = feed("");

        assertThat(page.get("items")).isEmpty();
        assertThat(page.get("nextCursor").isNull()).isTrue();
    }

    @Test
    void tamperedCursorIs400() throws Exception {
        call("?cursor=not-a-cursor").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARTNER_CURSOR_INVALID"));
        call("?cursor=" + java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("12:zzz".getBytes()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("PARTNER_CURSOR_INVALID"));
    }

    @Test
    void limitOutsideTheRangeIs400() throws Exception {
        call("?limit=0").andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("PARTNER_LIMIT_INVALID"));
        call("?limit=501").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARTNER_LIMIT_INVALID"));
        call("?limit=500").andExpect(status().isOk());
    }

    @Test
    void defaultPageSizeIs100() throws Exception {
        for (int i = 0; i < 101; i++) {
            finished(driver, i);
        }

        JsonNode page = feed("");

        assertThat(page.get("items")).hasSize(100);
        assertThat(page.get("hasMore").asBoolean()).isTrue();
    }

    @Test
    void aKeyOfAnotherCountryNeverSeesGabonSessions() throws Exception {
        finished(driver, 1);
        jdbc.update("update country set active = true where code = 'ML'");
        try {
            String maliKey = fixtures.keyFor("ML");

            String body = mvc.perform(get(PATH).header("X-Api-Key", maliKey)).andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            assertThat(mapper.readTree(body).get("items")).isEmpty();
        } finally {
            jdbc.update("update country set active = false where code = 'ML'");
        }
    }

    @Test
    void nonEmptyPagesAreAuditedWithTheCountOnly() throws Exception {
        finished(driver, 1);
        feed("");

        String detail = TenantContext.callAs("GA", () -> jdbc.queryForObject("select detail from audit_record "
                + "where action = 'PARTNER_FEED_READ' order by created_at desc limit 1", String.class));

        assertThat(detail).contains("items=1").doesNotContain(driver.toString());
    }

    @Test
    void feedWithoutKeyIs401() throws Exception {
        mvc.perform(get(PATH)).andExpect(status().isUnauthorized());
    }
}
