package com.evcharge.customer.wallet;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.account.application.port.PasswordHasher;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.TenantContext;
import com.evcharge.customer.wallet.application.WithdrawalService;
import com.evcharge.customer.wallet.domain.MobileOperator;
import com.evcharge.customer.wallet.domain.Withdrawal;
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
class WithdrawalDailyLimitIntegrationTest extends PostgresTestBase {

    private static final String PASSWORD = "CorrectHorse9";
    private static final long MAXIMUM = 200_000;
    private static final long DAILY_MAXIMUM = 500_000;
    private static final int ATTEMPTS = 4;

    @Autowired
    WithdrawalService withdrawals;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PasswordHasher hasher;

    @Autowired
    ScriptedPayoutGateway gateway;

    private UUID customer;

    @BeforeEach
    void richCustomer() {
        gateway.reset();
        customer = UUID.randomUUID();
        String phone = "+24177" + String.format("%06d", Math.abs(customer.hashCode()) % 1_000_000);
        jdbc.update("insert into customer (id, first_name, last_name, phone_e164, phone_verified_at, password_hash, "
                + "status, created_at) values (?, 'A', 'B', ?, ?, ?, 'ACTIVE', now())", customer, phone,
                new Timestamp(System.currentTimeMillis()), hasher.hash(PASSWORD));
        jdbc.update("insert into wallet (customer_id, currency, decimals, balance_minor, created_at) "
                + "values (?, 'XAF', 0, 2000000, now())", customer);
        jdbc.update("insert into ledger_entry (id, customer_id, type, amount_minor, idempotency_key, created_at) "
                + "values (?, ?, 'TOPUP', 2000000, ?, now())", UUID.randomUUID(), customer, "seed-" + customer);
    }

    private Withdrawal request(long amount, String key) {
        return TenantContext.callAs(TENANT,
                () -> withdrawals.request(customer, MobileOperator.AIRTEL, amount, PASSWORD, key));
    }

    private boolean granted(long amount, String key) {
        try {
            request(amount, key);
            return true;
        } catch (ApiException e) {
            assertThat(e.code()).isEqualTo("WITHDRAWAL_LIMIT_REACHED");
            return false;
        }
    }

    private long requestedTotal() {
        return jdbc.queryForObject("select coalesce(sum(amount_minor), 0) from withdrawal where customer_id = ?",
                Long.class, customer);
    }

    @Test
    void parallelRequestsCannotExceedTheDailyMaximum() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(ATTEMPTS);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<Boolean>> results = IntStream.range(0, ATTEMPTS).mapToObj(i -> pool.submit(() -> {
            go.await();
            return granted(MAXIMUM, "parallel-" + i);
        })).toList();
        go.countDown();
        long accepted = 0;
        for (Future<Boolean> result : results) {
            accepted += result.get() ? 1 : 0;
        }
        pool.shutdown();

        assertThat(accepted).isEqualTo(DAILY_MAXIMUM / MAXIMUM);
        assertThat(requestedTotal()).isLessThanOrEqualTo(DAILY_MAXIMUM);
        assertThat(jdbc.queryForObject("select balance_minor from wallet where customer_id = ?", Long.class,
                customer)).isEqualTo(2_000_000 - requestedTotal());
    }

    @Test
    void aReplayAtTheDailyMaximumReturnsTheEarlierWithdrawal() {
        request(MAXIMUM, "first");
        request(MAXIMUM, "second");
        Withdrawal last = request(DAILY_MAXIMUM - 2 * MAXIMUM, "last");

        Withdrawal replay = request(DAILY_MAXIMUM - 2 * MAXIMUM, "last");

        assertThat(replay.id()).isEqualTo(last.id());
        assertThat(requestedTotal()).isEqualTo(DAILY_MAXIMUM);
    }

    @Test
    void theNextRequestAfterTheDailyMaximumIsRefused() {
        request(MAXIMUM, "a");
        request(MAXIMUM, "b");
        request(DAILY_MAXIMUM - 2 * MAXIMUM, "c");

        assertThat(granted(1_000, "d")).isFalse();
        assertThat(requestedTotal()).isEqualTo(DAILY_MAXIMUM);
    }
}
