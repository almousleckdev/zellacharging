package com.evcharge.customer.wallet;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.shared.TenantContext;
import com.evcharge.customer.wallet.domain.PayoutOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(ScriptedGatewayConfiguration.class)
@TestPropertySource(properties = {
        "zella.payment.provider=scripted",
        "zella.wallet.withdrawal.enabled=true",
        "zella.wallet.withdrawal.recheck-after=0s",
        "zella.wallet.withdrawal.review-after=2h"})
class WithdrawalRefundIntegrationTest extends PostgresTestBase {

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    ScriptedPayoutGateway gateway;

    @Autowired
    WithdrawalReviews reviews;

    private UUID customer;
    private UUID withdrawal;
    private String reference;

    @BeforeEach
    void stuckWithdrawal() {
        gateway.reset();
        customer = UUID.randomUUID();
        withdrawal = UUID.randomUUID();
        reference = "WGA" + withdrawal.toString().replace("-", "").substring(0, 17).toUpperCase();
        jdbc.update("insert into customer (id, first_name, last_name, phone_e164, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, 'x', 'ACTIVE', now())", customer,
                "+24177" + String.format("%06d", Math.abs(customer.hashCode()) % 1_000_000));
        jdbc.update("insert into wallet (customer_id, currency, decimals, balance_minor, created_at) "
                + "values (?, 'XAF', 0, 6000, now())", customer);
        insertLedger("TOPUP", 10000);
        insertLedger("WITHDRAWAL", 4000);
        insertWithdrawal("REQUESTED");
    }

    private void insertLedger(String type, long amount) {
        jdbc.update("insert into ledger_entry (id, customer_id, type, amount_minor, idempotency_key, created_at) "
                + "values (?, ?, ?, ?, ?, now())", UUID.randomUUID(), customer, type, amount,
                type + ":" + UUID.randomUUID());
    }

    private void insertWithdrawal(String state) {
        jdbc.update("insert into withdrawal (id, customer_id, operator, msisdn, amount_minor, state, reference, "
                + "idempotency_key, created_at, updated_at) values (?, ?, 'AIRTEL', '+24177000000', 4000, ?, ?, ?, "
                + "now() - interval '1 hour', now() - interval '1 hour')", withdrawal, customer, state, reference,
                UUID.randomUUID().toString());
    }

    private void refund(String note) {
        TenantContext.runAs("GA", () -> reviews.refund(UUID.randomUUID(), withdrawal, note));
    }

    private long balance() {
        return jdbc.queryForObject("select balance_minor from wallet where customer_id = ?", Long.class, customer);
    }

    private String state() {
        return jdbc.queryForObject("select state from withdrawal where id = ?", String.class, withdrawal);
    }

    private int reversals() {
        return jdbc.queryForObject("select count(*) from ledger_entry where customer_id = ? and "
                + "type = 'WITHDRAWAL_REVERSAL'", Integer.class, customer);
    }

    @Test
    void aPayoutTheProviderNeverReceivedIsRefundedOnce() {
        refund("Provider refused the payout");

        assertThat(state()).isEqualTo("FAILED");
        assertThat(balance()).isEqualTo(10000);
        assertThat(reversals()).isOne();
        assertThat(jdbc.queryForObject("select failure_code from withdrawal where id = ?", String.class, withdrawal))
                .isEqualTo("REFUNDED");
        assertThat(jdbc.queryForObject("select review_note from withdrawal where id = ?", String.class, withdrawal))
                .isEqualTo("Provider refused the payout");
        assertThatThrownBy(() -> refund("again")).hasMessageContaining("cannot be refunded");
        assertThat(balance()).isEqualTo(10000);
        assertThat(reversals()).isOne();
    }

    @Test
    void aWithdrawalWaitingForManualReviewCanBeRefunded() {
        jdbc.update("update withdrawal set state = 'REVIEW' where id = ?", withdrawal);

        refund("Checked with the provider");

        assertThat(state()).isEqualTo("FAILED");
        assertThat(balance()).isEqualTo(10000);
    }

    @Test
    void aPayoutTheProviderIsProcessingIsNotRefunded() {
        gateway.reportStatus(reference, PayoutOutcome.pending("provider-1"));

        assertThatThrownBy(() -> refund("x")).hasMessageContaining("cannot be refunded");

        assertThat(state()).isEqualTo("REQUESTED");
        assertThat(balance()).isEqualTo(6000);
        assertThat(reversals()).isZero();
    }

    @Test
    void aPayoutTheProviderAlreadyPaidIsNotRefunded() {
        gateway.reportStatus(reference, PayoutOutcome.paid("provider-1"));

        assertThatThrownBy(() -> refund("x")).hasMessageContaining("cannot be refunded");

        assertThat(state()).isEqualTo("REQUESTED");
        assertThat(balance()).isEqualTo(6000);
    }

    @Test
    void aFailedPayoutIsRefunded() {
        gateway.reportStatus(reference, PayoutOutcome.failed("PAYOUT_DECLINED"));

        refund("Declined by the provider");

        assertThat(state()).isEqualTo("FAILED");
        assertThat(balance()).isEqualTo(10000);
    }

    @Test
    void nothingIsRefundedWhenTheProviderCannotBeReached() {
        gateway.failStatusWith(new IllegalStateException("timeout"));

        assertThatThrownBy(() -> refund("x")).hasMessageContaining("could not be reached");

        assertThat(state()).isEqualTo("REQUESTED");
        assertThat(balance()).isEqualTo(6000);
    }

    @Test
    void aWithdrawalStillWaitingForApprovalOrAlreadyPaidIsNotRefunded() {
        for (String state : new String[]{"PENDING_APPROVAL", "PAID", "FAILED"}) {
            jdbc.update("update withdrawal set state = ? where id = ?", state, withdrawal);

            assertThatThrownBy(() -> refund("x")).hasMessageContaining("cannot be refunded");
        }

        assertThat(balance()).isEqualTo(6000);
        assertThat(reversals()).isZero();
    }
}
