package com.evcharge.customer.admin;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.application.AdminLoginService;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.application.port.AdminTokenIssuer;
import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.shared.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AdminLockoutConcurrencyIntegrationTest extends PostgresTestBase {

    private static final int MAX_FAILURES = 5;

    @Autowired
    AdminLoginService logins;

    @Autowired
    AdminRepository admins;

    @Autowired
    AdminPasswordHasher passwords;

    @Autowired
    AdminTokenIssuer tokens;

    @Autowired
    JdbcTemplate jdbc;

    private AdminFixtures.Created admin;

    @BeforeEach
    void newAdmin() {
        admin = new AdminFixtures(admins, passwords, tokens).create(AdminRole.SUPER_ADMIN, Set.of());
    }

    private void guessInParallel(int guesses) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(guesses);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<String>> results = IntStream.range(0, guesses).mapToObj(i -> pool.submit(() -> {
            go.await();
            try {
                logins.login(admin.email(), "wrong-" + i);
            } catch (ApiException expected) {
                return expected.code();
            }
            return "ACCEPTED";
        })).toList();
        go.countDown();
        for (Future<String> result : results) {
            result.get();
        }
        pool.shutdown();
    }

    private int failedAttempts() {
        return jdbc.queryForObject("select failed_attempts from admin_user where id = ?", Integer.class, admin.id());
    }

    private Timestamp lockedUntil() {
        return jdbc.queryForObject("select locked_until from admin_user where id = ?", Timestamp.class, admin.id());
    }

    @Test
    void parallelWrongPasswordsAreAllCounted() throws Exception {
        guessInParallel(MAX_FAILURES - 1);

        assertThat(failedAttempts()).isEqualTo(MAX_FAILURES - 1);
        assertThat(lockedUntil()).isNull();
    }

    @Test
    void parallelWrongPasswordsStillReachTheLockout() throws Exception {
        guessInParallel(MAX_FAILURES);

        assertThat(lockedUntil()).isNotNull();
        assertThat(failedAttempts()).isZero();
    }
}
