package com.evcharge.customer.wallet;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import com.evcharge.customer.wallet.application.TopupReconciliationJob;
import com.evcharge.customer.wallet.domain.PaymentOutcome;
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
@TestPropertySource(properties = "zella.payment.provider=scripted")
class TopupSettlementIntegrationTest extends PostgresTestBase {

    private static final String BODY = "{\"amountMinor\":5000,\"msisdn\":\"+24177123456\",\"operator\":\"AIRTEL\"}";

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    ScriptedPaymentGateway gateway;

    @Autowired
    TopupReconciliationJob reconciliation;

    private UUID customer;

    @BeforeEach
    void newCustomer() {
        gateway.reset();
        customer = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, 'x', 'ACTIVE', now())", customer, customer + "@example.com");
    }

    private ResultActions topUp(String key) throws Exception {
        return mvc.perform(post("/v1/wallet/topups").with(TestTenants.customer(customer))
                .header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON).content(BODY));
    }

    private String reference() {
        return jdbc.queryForObject("select reference from topup where customer_id = ?", String.class, customer);
    }

    private long balance() {
        return jdbc.query("select balance_minor from wallet where customer_id = ?",
                (rs, n) -> rs.getLong(1), customer).stream().findFirst().orElse(0L);
    }

    private ResultActions callback(String reference) throws Exception {
        return mvc.perform(post("/v1/payments/callback").contentType(MediaType.APPLICATION_JSON)
                .content("{\"merchantReferenceId\":\"" + reference + "\",\"transactionId\":\"t1\",\"code\":\"200\"}"));
    }

    private int ledgerRows() {
        return jdbc.queryForObject("select count(*) from ledger_entry where customer_id = ? and type = 'TOPUP'",
                Integer.class, customer);
    }

    @Test
    void aPendingPaymentCreditsNothing() throws Exception {
        topUp("k1").andExpect(status().isCreated()).andExpect(jsonPath("$.state").value("PENDING"));
        assertThat(balance()).isZero();
        assertThat(ledgerRows()).isZero();
    }

    @Test
    void aConfirmedCallbackCreditsTheWalletExactlyOnce() throws Exception {
        topUp("k2");
        gateway.reportStatus(reference(), PaymentOutcome.succeeded("p-1", 5000));
        callback(reference()).andExpect(status().isOk());
        callback(reference()).andExpect(status().isOk());
        assertThat(balance()).isEqualTo(5000);
        assertThat(ledgerRows()).isOne();
    }

    @Test
    void aCallbackAloneNeverCreditsWithoutTheProviderConfirming() throws Exception {
        topUp("k3");
        callback(reference()).andExpect(status().isOk());
        assertThat(balance()).isZero();
    }

    @Test
    void unknownOrMalformedReferencesAreIgnoredSilently() throws Exception {
        callback("GA" + "A".repeat(18)).andExpect(status().isOk());
        callback("not-a-reference").andExpect(status().isOk());
        callback(null).andExpect(status().isOk());
        assertThat(balance()).isZero();
    }

    @Test
    void aReferenceFromAnotherCountryNeverReachesThisCustomersPayment() throws Exception {
        topUp("k4");
        gateway.reportStatus(reference(), PaymentOutcome.succeeded("p-2", 5000));
        callback("ML" + reference().substring(2)).andExpect(status().isOk());
        assertThat(balance()).isZero();
    }

    @Test
    void anUnknownOutcomeOfTheCollectionKeepsThePaymentPendingUntilTheProviderAnswers() throws Exception {
        gateway.failCollectionWith(new IllegalStateException("timeout"));
        topUp("k5").andExpect(status().isCreated()).andExpect(jsonPath("$.state").value("PENDING"));
        gateway.reportStatus(reference(), PaymentOutcome.succeeded("p-3", 5000));
        jdbc.update("update topup set created_at = now() - interval '10 minutes' where customer_id = ?", customer);
        reconciliation.run();
        assertThat(balance()).isEqualTo(5000);
    }

    @Test
    void aSecondRequestWithTheSameKeyDoesNotCollectTwice() throws Exception {
        topUp("same").andExpect(status().isCreated());
        topUp("same").andExpect(status().isCreated());
        assertThat(gateway.collected).hasSize(1);
        assertThat(jdbc.queryForObject("select count(*) from topup where customer_id = ?", Integer.class, customer))
                .isOne();
    }

    @Test
    void aSmallerConfirmedAmountCreditsOnlyWhatWasConfirmed() throws Exception {
        topUp("k6");
        gateway.reportStatus(reference(), PaymentOutcome.succeeded("p-4", 3000));
        callback(reference());
        assertThat(balance()).isEqualTo(3000);
    }

    @Test
    void aDeclinedPaymentFailsAndNeverCredits() throws Exception {
        topUp("k7");
        gateway.reportStatus(reference(), PaymentOutcome.failed("PAYMENT_DECLINED"));
        callback(reference());
        mvc.perform(get("/v1/wallet/topups/" + jdbc.queryForObject("select id from topup where customer_id = ?",
                        UUID.class, customer)).with(TestTenants.customer(customer)))
                .andExpect(jsonPath("$.state").value("FAILED")).andExpect(jsonPath("$.failureCode")
                        .value("PAYMENT_DECLINED"));
        assertThat(balance()).isZero();
    }

    @Test
    void anOldPendingPaymentExpiresAndALateConfirmationStillCredits() throws Exception {
        topUp("k8");
        jdbc.update("update topup set created_at = now() - interval '40 minutes' where customer_id = ?", customer);
        reconciliation.run();
        assertThat(jdbc.queryForObject("select state from topup where customer_id = ?", String.class, customer))
                .isEqualTo("EXPIRED");
        gateway.reportStatus(reference(), PaymentOutcome.succeeded("p-5", 5000));
        reconciliation.run();
        assertThat(balance()).isEqualTo(5000);
        assertThat(jdbc.queryForObject("select state from topup where customer_id = ?", String.class, customer))
                .isEqualTo("SUCCEEDED");
    }

    @Test
    void aTopupCannotBeReadByAnotherCustomer() throws Exception {
        topUp("k9");
        UUID id = jdbc.queryForObject("select id from topup where customer_id = ?", UUID.class, customer);
        mvc.perform(get("/v1/wallet/topups/" + id).with(TestTenants.anyCustomer()))
                .andExpect(status().isNotFound());
    }

    @Test
    void aSecondPaymentIsRefusedWhileTheFirstIsStillBeingVerified() throws Exception {
        topUp("first").andExpect(status().isCreated());
        topUp("second").andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("TOPUP_PENDING"));
        assertThat(gateway.collected).hasSize(1);
    }
}
