package com.evcharge.customer.partner;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.partner.domain.EvidenceReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EvidenceFeedIntegrationTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PartnerKeys keys;

    private PartnerFixtures fixtures;
    private String key;
    private UUID driver;

    @BeforeEach
    void prepare() {
        fixtures = new PartnerFixtures(jdbc, keys);
        key = fixtures.keyFor("GA");
        driver = customer();
        fixtures.suspend("GA", driver, 69000);
    }

    private UUID customer() {
        return fixtures.customer("GA", "A", "B", UUID.randomUUID() + "@example.com", "+24106" + (100000
                + java.util.concurrent.ThreadLocalRandom.current().nextInt(899999)), "ACTIVE");
    }

    private ResultActions feed(String query) throws Exception {
        return mvc.perform(get("/v1/partners/ndzela/evidence?customerId=" + driver + query).header("X-Api-Key", key));
    }

    @Test
    void thePartnerSeesEvidenceOnlyOfItsLinkedDrivers() throws Exception {
        UUID stranger = customer();
        fixtures.evidence("GA", driver, customer(), false, 5000, "k-1");
        fixtures.evidence("GA", stranger, customer(), false, 7000, "k-2");

        feed("").andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].suspendedCustomerId").value(driver.toString()))
                .andExpect(jsonPath("$.items[0].amountMinor").value(5000))
                .andExpect(jsonPath("$.items[0].kind").value("TOPUP_FROM_SUSPENDED_NUMBER"));
        mvc.perform(get("/v1/partners/ndzela/evidence?customerId=" + stranger).header("X-Api-Key", key))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void thePartnerNeverSeesTheReceivingCustomerUnlessLinked() throws Exception {
        UUID linkedHelper = customer();
        UUID plainHelper = customer();
        fixtures.link("GA", linkedHelper);
        fixtures.evidence("GA", driver, linkedHelper, true, 1000, "k-3");
        fixtures.evidence("GA", driver, plainHelper, false, 2000, "k-4");

        String body = feed("").andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.amountMinor==1000)].receivingCustomerId")
                        .value(linkedHelper.toString()))
                .andExpect(jsonPath("$.items[?(@.amountMinor==1000)].receivingIsLinkedDriver").value(true))
                .andExpect(jsonPath("$.items[?(@.amountMinor==2000)].receivingIsLinkedDriver").value(false))
                .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain(plainHelper.toString()).doesNotContain("@").doesNotContain("+241");
        assertThat(body).contains(EvidenceReference.of(plainHelper));
    }

    @Test
    void pagingResumesFromTheCursor() throws Exception {
        for (int i = 0; i < 3; i++) {
            fixtures.evidence("GA", driver, customer(), false, 100 + i, "k-p" + i);
        }

        String cursor = com.jayway.jsonpath.JsonPath.read(feed("&limit=2").andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2)).andExpect(jsonPath("$.hasMore").value(true))
                .andReturn().getResponse().getContentAsString(), "$.nextCursor");

        feed("&limit=2&cursor=" + cursor).andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1)).andExpect(jsonPath("$.hasMore").value(false))
                .andExpect(jsonPath("$.items[0].amountMinor").value(102));
    }

    @Test
    void anEventStillInsideTheFeedLagIsNotServedYetSoNoRowIsEverSkipped() throws Exception {
        fixtures.evidence("GA", driver, customer(), false, 700, "k-late", 0);
        fixtures.evidence("GA", driver, customer(), false, 701, "k-old", 120);

        feed("").andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].amountMinor").value(701));
    }

    @Test
    void aBadCursorIsRefused() throws Exception {
        feed("&cursor=not-a-cursor").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARTNER_CURSOR_INVALID"));
    }
}
