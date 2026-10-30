package com.evcharge.customer.wallet;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.account.application.port.PasswordHasher;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.TenantContext;
import com.evcharge.customer.wallet.application.WithdrawalService;
import com.evcharge.customer.wallet.domain.MobileOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(ScriptedGatewayConfiguration.class)
@TestPropertySource(properties = {
        "zella.payment.provider=scripted",
        "zella.wallet.withdrawal.enabled=true"})
class WithdrawalBalanceRaceIntegrationTest extends PostgresTestBase {

    private static final String PASSWORD = "CorrectHorse9";
    private static final long BALANCE = 10_000;
    private static final long AMOUNT = 3_000;
    private static final int ATTEMPTS = 8;

    @Autowired
    WithdrawalService withdrawals;

    @Autowired
    WithdrawalReviews reviews;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PasswordHasher hasher;

    @Autowired
    ScriptedPayoutGateway gateway;

    private UUID customer;

    @BeforeEach
    void customerWithTenThousand() {
        gateway.reset();
        customer = UUID.randomUUID();
        String phone = "+24177" + String.format("%06d", Math.abs(customer.hashCode()) % 1_000_000);
        jdbc.update("insert into customer (id, first_name, last_name, phone_e164, phone_verified_at, password_hash, "
                + "status, created_at) values (?, 'A', 'B', ?, ?, ?, 'ACTIVE', now())", customer, phone,
                new Timestamp(System.currentTimeMillis()), hasher.hash(PASSWORD));
        jdbc.update("insert into topup (id, customer_id, operator, msisdn, amount_minor, state, idempotency_key, "
                + "reference, created_at, updated_at) values (?, ?, 'AIRTEL', ?, ?, 'SUCCEEDED', ?, ?, now(), now())",
                UUID.randomUUID(), customer, phone, BALANCE, "seed-" + customer,
                "GAP" + customer.toString().replace("-", "").substring(0, 17).toUpperCase());
        jdbc.update("insert into wallet (customer_id, currency, decimals, balance_minor, created_at) "
                + "values (?, 'XAF', 0, ?, now())", customer, BALANCE);
        jdbc.update("insert into ledger_entry (id, customer_id, type, amount_minor, idempotency_key, created_at) "
                + "values (?, ?, 'TOPUP', ?, ?, now())", UUID.randomUUID(), customer, BALANCE, "seed-" + customer);
    }

    private boolean attempt(String key) {
        try {
            TenantContext.callAs(TENANT,
                    () -> withdrawals.request(customer, MobileOperator.AIRTEL, null, AMOUNT, PASSWORD, key));
            return true;
        } catch (ApiException e) {
            assertThat(e.code()).isEqualTo("INSUFFICIENT_FUNDS");
            return false;
        }
    }

    private long inParallel(java.util.function.IntFunction<String> keys) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(ATTEMPTS);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<Boolean>> results = IntStream.range(0, ATTEMPTS).mapToObj(i -> pool.submit(() -> {
            go.await();
            return attempt(keys.apply(i));
        })).toList();
        go.countDown();
        long accepted = 0;
        for (Future<Boolean> result : results) {
            accepted += result.get() ? 1 : 0;
        }
        pool.shutdown();
        return accepted;
    }

    private void approveAll() {
        jdbc.queryForList("select id from withdrawal where customer_id = ?", UUID.class, customer)
                .forEach(id -> TenantContext.runAs(TENANT, () -> reviews.approve(UUID.randomUUID(), id, null)));
    }

    private long balance() {
        return jdbc.queryForObject("select balance_minor from wallet where customer_id = ?", Long.class, customer);
    }

    @Test
    void parallelWithdrawalsCanNeverSpendMoreThanTheBalance() throws Exception {
        long accepted = inParallel(i -> "race-" + i);

        assertThat(accepted).isEqualTo(BALANCE / AMOUNT);
        assertThat(balance()).isEqualTo(BALANCE - accepted * AMOUNT).isGreaterThanOrEqualTo(0);
        approveAll();
        assertThat(gateway.sent).hasSize((int) accepted);
    }

    @Test
    void parallelRetriesWithOneKeyPayOnlyOnce() throws Exception {
        inParallel(i -> "same-key");

        assertThat(jdbc.queryForObject("select count(*) from withdrawal where customer_id = ?", Integer.class,
                customer)).isOne();
        assertThat(balance()).isEqualTo(BALANCE - AMOUNT);
        approveAll();
        assertThat(gateway.sent).hasSize(1);
    }
}
