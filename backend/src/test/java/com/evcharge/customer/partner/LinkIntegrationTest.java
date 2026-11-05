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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LinkIntegrationTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PartnerKeys keys;

    private PartnerFixtures fixtures;
    private String key;

    @BeforeEach
    void prepare() {
        fixtures = new PartnerFixtures(jdbc, keys);
        key = fixtures.keyFor("GA");
    }

    private ResultActions send(String apiKey, MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.header("X-Api-Key", apiKey));
    }

    private static String path(UUID customer) {
        return "/v1/partners/ndzela/links/" + customer;
    }

    private UUID customer(String country, String status) {
        return fixtures.customer(country, "A", "B", UUID.randomUUID() + "@example.com", null, status);
    }

    private int links(UUID customer) {
        return TenantContext.callAs("GA", () -> jdbc.queryForObject(
                "select count(*) from partner_link where customer_id = ?", Integer.class, customer));
    }

    @Test
    void putLinksAnActiveCustomerAndTwiceStillMeansOneRow() throws Exception {
        UUID customer = customer("GA", "ACTIVE");

        send(key, put(path(customer))).andExpect(status().isNoContent());
        send(key, put(path(customer))).andExpect(status().isNoContent());

        assertThat(links(customer)).isEqualTo(1);
    }

    @Test
    void putUnknownInactiveOrOtherCountryCustomerIs404() throws Exception {
        send(key, put(path(UUID.randomUUID()))).andExpect(status().isNotFound());
        send(key, put(path(customer("GA", "BLOCKED")))).andExpect(status().isNotFound());
        send(key, put(path(customer("ML", "ACTIVE")))).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void deleteRemovesAndIsIdempotentEvenForUnknownCustomers() throws Exception {
        UUID customer = customer("GA", "ACTIVE");
        send(key, put(path(customer))).andExpect(status().isNoContent());

        send(key, delete(path(customer))).andExpect(status().isNoContent());
        send(key, delete(path(customer))).andExpect(status().isNoContent());
        send(key, delete(path(UUID.randomUUID()))).andExpect(status().isNoContent());

        assertThat(links(customer)).isZero();
    }

    @Test
    void linksAreAuditedOnceEachWithoutPersonalData() throws Exception {
        UUID customer = customer("GA", "ACTIVE");
        send(key, put(path(customer))).andExpect(status().isNoContent());
        send(key, put(path(customer))).andExpect(status().isNoContent());
        send(key, delete(path(customer))).andExpect(status().isNoContent());
        send(key, delete(path(customer))).andExpect(status().isNoContent());

        List<String> actions = TenantContext.callAs("GA", () -> jdbc.queryForList(
                "select action from audit_record where subject = ? order by created_at", String.class,
                customer.toString()));

        assertThat(actions).containsExactly("PARTNER_LINK_CREATED", "PARTNER_LINK_REMOVED");
    }

    @Test
    void aMaliKeyCannotLinkAGabonCustomer() throws Exception {
        jdbc.update("update country set active = true where code = 'ML'");
        try {
            String maliKey = fixtures.keyFor("ML");

            send(maliKey, put(path(customer("GA", "ACTIVE")))).andExpect(status().isNotFound());
        } finally {
            jdbc.update("update country set active = false where code = 'ML'");
        }
    }

    @Test
    void linksNeedAKey() throws Exception {
        mvc.perform(put(path(UUID.randomUUID()))).andExpect(status().isUnauthorized());
        mvc.perform(delete(path(UUID.randomUUID()))).andExpect(status().isUnauthorized());
    }
}
