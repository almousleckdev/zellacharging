package com.evcharge.customer.partner;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SuspensionIntegrationTest extends PostgresTestBase {

    private static final String BODY = "{\"owedMinor\":69000,\"currency\":\"XAF\",\"decimals\":0}";

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

    private static String suspension(UUID customer) {
        return "/v1/partners/ndzela/customers/" + customer + "/suspension";
    }

    private UUID linkedCustomer(String phone) throws Exception {
        UUID customer = fixtures.customer("GA", "A", "B", UUID.randomUUID() + "@example.com", phone, "ACTIVE");
        send(put("/v1/partners/ndzela/links/" + customer)).andExpect(status().isNoContent());
        return customer;
    }

    private void aTopupFrom(UUID customer, String msisdn) {
        UUID id = UUID.randomUUID();
        TenantContext.runAs("GA", () -> jdbc.update("insert into topup (id, customer_id, operator, msisdn, "
                + "amount_minor, state, idempotency_key, created_at, updated_at, reference) "
                + "values (?, ?, 'AIRTEL', ?, 5000, 'SUCCEEDED', ?, now(), now(), ?)", id, customer, msisdn,
                id.toString(), "GA" + id.toString().replace("-", "")));
    }

    private String statusOf(UUID customer) {
        return TenantContext.callAs("GA", () -> jdbc.queryForObject(
                "select status from customer where id = ?", String.class, customer));
    }

    private List<String> numbersOf(UUID customer) {
        return TenantContext.callAs("GA", () -> jdbc.queryForList(
                "select msisdn from suspended_number where customer_id = ? order by msisdn", String.class, customer));
    }

    private Long owedOf(UUID customer) {
        return TenantContext.callAs("GA", () -> jdbc.queryForObject(
                "select owed_minor from partner_suspension where customer_id = ? and restored_at is null",
                Long.class, customer));
    }

    @Test
    void suspendsALinkedCustomerAndStoresTheirNumbers() throws Exception {
        UUID customer = linkedCustomer("+24106000001");
        aTopupFrom(customer, "+24107000002");

        send(put(suspension(customer)).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNoContent());

        assertThat(statusOf(customer)).isEqualTo("BLOCKED");
        assertThat(owedOf(customer)).isEqualTo(69000L);
        assertThat(numbersOf(customer)).containsExactly("+24106000001", "+24107000002");
    }

    @Test
    void suspendingTwiceChangesNothing() throws Exception {
        UUID customer = linkedCustomer("+24106000003");

        send(put(suspension(customer)).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNoContent());
        send(put(suspension(customer)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"owedMinor\":92000,\"currency\":\"XAF\",\"decimals\":0}"))
                .andExpect(status().isNoContent());

        assertThat(statusOf(customer)).isEqualTo("BLOCKED");
        assertThat(owedOf(customer)).isEqualTo(92000L);
        assertThat(numbersOf(customer)).hasSize(1);
    }

    @Test
    void restoreReactivatesAndClearsTheNumbers() throws Exception {
        UUID customer = linkedCustomer("+24106000004");
        send(put(suspension(customer)).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNoContent());

        send(delete(suspension(customer))).andExpect(status().isNoContent());
        send(delete(suspension(customer))).andExpect(status().isNoContent());

        assertThat(statusOf(customer)).isEqualTo("ACTIVE");
        assertThat(numbersOf(customer)).isEmpty();
    }

    @Test
    void restoreNeverLiftsABlockTheCustomerAlreadyHad() throws Exception {
        UUID customer = linkedCustomer("+24106000005");
        TenantContext.runAs("GA", () -> jdbc.update("update customer set status = 'BLOCKED' where id = ?", customer));
        send(put(suspension(customer)).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNoContent());

        send(delete(suspension(customer))).andExpect(status().isNoContent());

        assertThat(statusOf(customer)).isEqualTo("BLOCKED");
        assertThat(numbersOf(customer)).isEmpty();
    }

    @Test
    void anUnlinkedCustomerIsRefusedWithNotFound() throws Exception {
        UUID customer = fixtures.customer("GA", "A", "B", UUID.randomUUID() + "@example.com", null, "ACTIVE");

        send(put(suspension(customer)).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());

        assertThat(statusOf(customer)).isEqualTo("ACTIVE");
    }
}
