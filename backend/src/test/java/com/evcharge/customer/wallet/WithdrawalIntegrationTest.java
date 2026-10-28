package com.evcharge.customer.wallet;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import com.evcharge.customer.account.application.port.PasswordHasher;
import com.evcharge.customer.wallet.application.LedgerReconciliationJob;
import com.evcharge.customer.wallet.application.WithdrawalReconciliationJob;
import com.evcharge.customer.wallet.domain.PayoutOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ScriptedGatewayConfiguration.class)
@TestPropertySource(properties = {
        "zella.payment.provider=scripted",
        "zella.wallet.withdrawal.enabled=true",
        "zella.wallet.withdrawal.recheck-after=0s",
        "zella.wallet.withdrawal.review-after=2h"})
class WithdrawalIntegrationTest extends PostgresTestBase {

    private static final String PASSWORD = "CorrectHorse9";

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PasswordHasher hasher;

    @Autowired
    ScriptedPayoutGateway gateway;

    @Autowired
    WithdrawalReconciliationJob reconciliation;

    @Autowired
    LedgerReconciliationJob ledgerCheck;

    private UUID customer;

    @BeforeEach
    void newCustomer() {
        gateway.reset();
        customer = customerWith(10000, true);
    }

    private UUID customerWith(long balance, boolean paidNumber) {
        UUID id = UUID.randomUUID();
        String phone = "+24177" + String.format("%06d", Math.abs(id.hashCode()) % 1_000_000);
        jdbc.update("insert into customer (id, first_name, last_name, phone_e164, phone_verified_at, password_hash, "
                + "status, created_at) values (?, 'A', 'B', ?, ?, ?, 'ACTIVE', now())", id, phone,
                paidNumber ? new java.sql.Timestamp(System.currentTimeMillis()) : null, hasher.hash(PASSWORD));
        if (paidNumber) {
            jdbc.update("insert into topup (id, customer_id, operator, msisdn, amount_minor, state, idempotency_key, "
                    + "reference, created_at, updated_at) values (?, ?, 'AIRTEL', ?, ?, 'SUCCEEDED', ?, ?, now(), now())",
                    UUID.randomUUID(), id, phone, balance, "seed-" + id, "GAP" + id.toString().replace("-", "")
                            .substring(0, 17).toUpperCase());
        }
        jdbc.update("insert into wallet (customer_id, currency, decimals, balance_minor, created_at) "
                + "values (?, 'XAF', 0, ?, now())", id, balance);
        jdbc.update("insert into ledger_entry (id, customer_id, type, amount_minor, idempotency_key, created_at) "
                + "values (?, ?, 'TOPUP', ?, ?, now())", UUID.randomUUID(), id, balance, "seed-" + id);
        return id;
    }

    private ResultActions withdraw(UUID who, long amount, String password, String key) throws Exception {
        var request = post("/v1/wallet/withdrawals").with(TestTenants.customer(who))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amountMinor\":" + amount + ",\"operator\":\"AIRTEL\",\"password\":\"" + password + "\"}");
        return mvc.perform(key == null ? request : request.header("Idempotency-Key", key));
    }

    private long balance(UUID who) {
        return jdbc.queryForObject("select balance_minor from wallet where customer_id = ?", Long.class, who);
    }

    private String stateOf(UUID who) {
        return jdbc.queryForObject("select state from withdrawal where customer_id = ?", String.class, who);
    }

    private String referenceOf(UUID who) {
        return jdbc.queryForObject("select reference from withdrawal where customer_id = ?", String.class, who);
    }

    private int rows(String table, UUID who) {
        return jdbc.queryForObject("select count(*) from " + table + " where customer_id = ?", Integer.class, who);
    }

    @Test
    void aWithdrawalDebitsTheWalletAtOnceAndIsPaidOnlyWhenTheProviderConfirms() throws Exception {
        withdraw(customer, 4000, PASSWORD, "w1").andExpect(status().isCreated())
                .andExpect(jsonPath("$.state").value("PROCESSING"));
        assertThat(balance(customer)).isEqualTo(6000);
        assertThat(gateway.sent).hasSize(1);
        assertThat(gateway.sent.get(0).msisdn()).startsWith("+24177");
        gateway.reportStatus(referenceOf(customer), PayoutOutcome.paid("pp-1"));
        reconciliation.run();
        assertThat(stateOf(customer)).isEqualTo("PAID");
        assertThat(ledgerCheck.check("GA")).noneMatch(item -> item.customerId().equals(customer));
        assertThat(jdbc.queryForObject("select count(*) from audit_record where actor_id = ? and action = 'WITHDRAWAL_REQUESTED'",
                Integer.class, customer)).isOne();
    }

    @Test
    void theWrongPasswordMovesNothing() throws Exception {
        withdraw(customer, 4000, "WrongPassword1", "w2").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WRONG_PASSWORD"));
        assertThat(balance(customer)).isEqualTo(10000);
        assertThat(rows("withdrawal", customer)).isZero();
    }

    @Test
    void moneyOnlyGoesBackToANumberThatPaidASuccessfulTopup() throws Exception {
        UUID unverified = customerWith(10000, false);
        withdraw(unverified, 4000, PASSWORD, "w3").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("NO_PAYOUT_NUMBER"));
        assertThat(balance(unverified)).isEqualTo(10000);
    }

    @Test
    void youCannotWithdrawMoreThanYouHaveOrWhatIsReservedForACharge() throws Exception {
        withdraw(customer, 20000, PASSWORD, "w4").andExpect(status().isPaymentRequired());
        jdbc.update("update wallet set reserved_minor = 8000 where customer_id = ?", customer);
        withdraw(customer, 4000, PASSWORD, "w5").andExpect(status().isPaymentRequired());
        assertThat(balance(customer)).isEqualTo(10000);
        assertThat(rows("withdrawal", customer)).isZero();
    }

    @Test
    void aRefusedPayoutIsReversedImmediately() throws Exception {
        gateway.answerWith(PayoutOutcome.failed("PROVIDER_REFUSED"));
        withdraw(customer, 4000, PASSWORD, "w6").andExpect(status().isCreated())
                .andExpect(jsonPath("$.state").value("FAILED"));
        assertThat(balance(customer)).isEqualTo(10000);
        assertThat(rows("ledger_entry", customer)).isEqualTo(3);
    }

    @Test
    void aFailureConfirmedLaterIsReversedExactlyOnce() throws Exception {
        withdraw(customer, 4000, PASSWORD, "w7");
        gateway.reportStatus(referenceOf(customer), PayoutOutcome.failed("DECLINED"));
        reconciliation.run();
        reconciliation.run();
        assertThat(stateOf(customer)).isEqualTo("FAILED");
        assertThat(balance(customer)).isEqualTo(10000);
        assertThat(jdbc.queryForObject("select count(*) from ledger_entry where customer_id = ? and "
                + "type = 'WITHDRAWAL_REVERSAL'", Integer.class, customer)).isOne();
    }

    @Test
    void anUnknownOutcomeKeepsTheFundsHeldAndGoesToManualReview() throws Exception {
        gateway.failWith(new IllegalStateException("timeout"));
        withdraw(customer, 4000, PASSWORD, "w8").andExpect(status().isCreated())
                .andExpect(jsonPath("$.state").value("REQUESTED"));
        assertThat(balance(customer)).isEqualTo(6000);
        jdbc.update("update withdrawal set created_at = now() - interval '3 hours' where customer_id = ?", customer);
        reconciliation.run();
        assertThat(stateOf(customer)).isEqualTo("REVIEW");
        assertThat(balance(customer)).isEqualTo(6000);
    }

    @Test
    void repeatingTheSameKeyPaysOnce() throws Exception {
        withdraw(customer, 4000, PASSWORD, "same").andExpect(status().isCreated());
        withdraw(customer, 4000, PASSWORD, "same").andExpect(status().isCreated());
        assertThat(gateway.sent).hasSize(1);
        assertThat(balance(customer)).isEqualTo(6000);
        assertThat(rows("withdrawal", customer)).isOne();
    }

    @Test
    void limitsAreEnforced() throws Exception {
        withdraw(customer, 100, PASSWORD, "l1").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_AMOUNT"));
        withdraw(customer, 300_000, PASSWORD, "l2").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_AMOUNT"));
        UUID rich = customerWith(2_000_000, true);
        withdraw(rich, 200_000, PASSWORD, "l3").andExpect(status().isCreated());
        withdraw(rich, 200_000, PASSWORD, "l4").andExpect(status().isCreated());
        withdraw(rich, 200_000, PASSWORD, "l5").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WITHDRAWAL_LIMIT_REACHED"));
    }

    @Test
    void aKeyIsRequiredAndOthersCannotReadYourWithdrawal() throws Exception {
        withdraw(customer, 4000, PASSWORD, null).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REQUIRED"));
        withdraw(customer, 4000, PASSWORD, "w9");
        UUID id = jdbc.queryForObject("select id from withdrawal where customer_id = ?", UUID.class, customer);
        mvc.perform(get("/v1/wallet/withdrawals/" + id).with(TestTenants.anyCustomer()))
                .andExpect(status().isNotFound());
    }

    @Test
    void theLedgerCheckFlagsAWalletThatNoLongerMatchesItsEntries() {
        jdbc.update("update wallet set balance_minor = balance_minor + 500 where customer_id = ?", customer);
        assertThat(ledgerCheck.check("GA")).anyMatch(item -> item.customerId().equals(customer)
                && item.balanceMinor() == item.expectedBalanceMinor() + 500);
        jdbc.update("update wallet set balance_minor = balance_minor - 500 where customer_id = ?", customer);
        assertThat(ledgerCheck.check("GA")).noneMatch(item -> item.customerId().equals(customer));
    }
}
