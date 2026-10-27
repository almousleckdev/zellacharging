package com.evcharge.customer.wallet;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WalletHistoryIntegrationTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    private UUID customer;
    private UUID topupEntry;
    private UUID withdrawalEntry;
    private String reference;

    @BeforeEach
    void seed() {
        customer = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, 'x', 'ACTIVE', now())", customer, customer + "@example.com");
        jdbc.update("insert into wallet (customer_id, currency, decimals, balance_minor, created_at) "
                + "values (?, 'XAF', 0, 3000, now())", customer);
        UUID topup = UUID.randomUUID();
        reference = "GA" + topup.toString().replace("-", "").substring(0, 18).toUpperCase();
        jdbc.update("insert into topup (id, customer_id, operator, msisdn, amount_minor, state, idempotency_key, "
                + "reference, created_at, updated_at) values (?, ?, 'MOOV', '+24162820745', 5000, 'SUCCEEDED', ?, "
                + "?, now(), now())", topup, customer, "k-" + topup, reference);
        topupEntry = UUID.randomUUID();
        jdbc.update("insert into ledger_entry (id, customer_id, type, amount_minor, topup_id, idempotency_key, "
                + "created_at) values (?, ?, 'TOPUP', 5000, ?, ?, now() - interval '2 minutes')", topupEntry, customer,
                topup, "topup-" + topup);
        withdrawalEntry = UUID.randomUUID();
        jdbc.update("insert into ledger_entry (id, customer_id, type, amount_minor, idempotency_key, created_at) "
                + "values (?, ?, 'WITHDRAWAL', 2000, ?, now())", withdrawalEntry, customer,
                "withdrawal:" + UUID.randomUUID());
    }

    @Test
    void aTopupDetailShowsTheNumberOperatorReferenceAndBalanceAfter() throws Exception {
        mvc.perform(get("/v1/wallet/transactions/" + topupEntry).with(TestTenants.customer(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("TOPUP"))
                .andExpect(jsonPath("$.operator").value("MOOV"))
                .andExpect(jsonPath("$.msisdn").value("+24162820745"))
                .andExpect(jsonPath("$.reference").value(reference))
                .andExpect(jsonPath("$.balanceAfterMinor").value(5000));
    }

    @Test
    void theBalanceAfterFollowsTheLedgerOrder() throws Exception {
        mvc.perform(get("/v1/wallet/transactions/" + withdrawalEntry).with(TestTenants.customer(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceAfterMinor").value(3000));
    }

    @Test
    void aDeletedEntryDisappearsFromTheListButStaysInTheLedger() throws Exception {
        mvc.perform(delete("/v1/wallet/transactions/" + topupEntry).with(TestTenants.customer(customer)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/v1/wallet/transactions").with(TestTenants.customer(customer)))
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/v1/wallet/transactions/" + topupEntry).with(TestTenants.customer(customer)))
                .andExpect(status().isNotFound());
        Integer kept = jdbc.queryForObject("select count(*) from ledger_entry where id = ?", Integer.class, topupEntry);
        org.assertj.core.api.Assertions.assertThat(kept).isEqualTo(1);
    }

    @Test
    void anotherCustomerCannotSeeOrDeleteMyEntry() throws Exception {
        mvc.perform(get("/v1/wallet/transactions/" + topupEntry).with(TestTenants.anyCustomer()))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/v1/wallet/transactions/" + topupEntry).with(TestTenants.anyCustomer()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/v1/wallet/transactions/" + topupEntry).with(TestTenants.customer(customer)))
                .andExpect(status().isOk());
    }
}
