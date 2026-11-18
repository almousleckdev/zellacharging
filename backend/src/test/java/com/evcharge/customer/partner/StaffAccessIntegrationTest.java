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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StaffAccessIntegrationTest extends PostgresTestBase {

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

    private ResultActions send(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.header("X-Api-Key", key));
    }

    private static String staff(UUID customer) {
        return "/v1/partners/ndzela/staff-access/" + customer;
    }

    private UUID linked() throws Exception {
        UUID customer = fixtures.customer("GA", "A", "B", UUID.randomUUID() + "@example.com", null, "ACTIVE");
        send(put("/v1/partners/ndzela/links/" + customer)).andExpect(status().isNoContent());
        return customer;
    }

    private String category(UUID customer) {
        return TenantContext.callAs("GA", () -> jdbc.queryForList(
                "select category from customer_complimentary where customer_id = ?", String.class, customer))
                .stream().findFirst().orElse(null);
    }

    private static MockHttpServletRequestBuilder grant(UUID customer, String reason) {
        return put(staff(customer)).contentType("application/json").content("{\"reason\":\"" + reason + "\"}");
    }

    @Test
    void aLinkedCustomerIsGivenFreeStaffChargingAndItCanBeRemoved() throws Exception {
        UUID customer = linked();

        send(grant(customer, "Personnel NDZela - Toyota")).andExpect(status().isNoContent());
        assertThat(category(customer)).isEqualTo("NDZELA_STAFF");

        send(delete(staff(customer))).andExpect(status().isNoContent());
        assertThat(category(customer)).isNull();
    }

    @Test
    void aCustomerWhoIsNotLinkedIsRefused() throws Exception {
        UUID customer = fixtures.customer("GA", "A", "B", UUID.randomUUID() + "@example.com", null, "ACTIVE");

        send(grant(customer, "Personnel")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CUSTOMER_NOT_LINKED"));
        assertThat(category(customer)).isNull();
    }

    @Test
    void aReasonIsRequired() throws Exception {
        send(grant(linked(), "  ")).andExpect(status().isBadRequest());
    }

    @Test
    void removingNeverTouchesAnotherKindOfFreeAccess() throws Exception {
        UUID customer = linked();
        TenantContext.runAs("GA", () -> jdbc.update("insert into customer_complimentary (customer_id, granted_by, "
                + "reason, category, granted_at) values (?, ?, 'DG', 'OTHER', now())", customer, UUID.randomUUID()));

        send(delete(staff(customer))).andExpect(status().isNoContent());

        assertThat(category(customer)).isEqualTo("OTHER");
    }

    @Test
    void withoutAKeyTheCallIsRefused() throws Exception {
        mvc.perform(put(staff(UUID.randomUUID())).contentType("application/json").content("{\"reason\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }
}
