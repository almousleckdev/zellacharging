package com.evcharge.customer.wallet;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class WalletConcurrencyIntegrationTest extends PostgresTestBase {

    private static final int ATTEMPTS = 8;

    @Autowired
    WalletFunding funding;

    @Autowired
    JdbcTemplate jdbc;

    private UUID customerWith(long balance) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, 'x', 'ACTIVE', now())", id, id + "@example.com");
        jdbc.update("insert into wallet (customer_id, currency, decimals, balance_minor, created_at) "
                + "values (?, 'XOF', 0, ?, now())", id, balance);
        return id;
    }

    private boolean reserves(UUID customer) {
        return TenantContext.callAs(TENANT, () -> {
            try {
                funding.reserve(customer, UUID.randomUUID(), 2000);
                return true;
            } catch (ApiException e) {
                return false;
            }
        });
    }

    @Test
    void concurrentStartsCannotSpendTheSameBalanceTwice() throws Exception {
        UUID customer = customerWith(5000);
        ExecutorService pool = Executors.newFixedThreadPool(ATTEMPTS);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();
        for (int i = 0; i < ATTEMPTS; i++) {
            Callable<Boolean> attempt = () -> {
                go.await();
                return reserves(customer);
            };
            results.add(pool.submit(attempt));
        }
        go.countDown();
        long granted = 0;
        for (Future<Boolean> result : results) {
            granted += result.get() ? 1 : 0;
        }
        pool.shutdown();

        assertThat(granted).isEqualTo(2);
        assertThat(jdbc.queryForObject("select reserved_minor from wallet where customer_id = ?", Long.class,
                customer)).isEqualTo(4000);
        assertThat(jdbc.queryForObject("select count(*) from funding_hold where customer_id = ?", Integer.class,
                customer)).isEqualTo(2);
    }
}
