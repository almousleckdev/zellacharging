package com.evcharge.customer.account;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import com.evcharge.customer.account.application.port.PasswordHasher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PasswordProtectionIntegrationTest extends PostgresTestBase {

    private static final String PASSWORD = "OldPass123";
    private static final int MAX_FAILURES = 5;

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PasswordHasher hasher;

    private UUID customer() {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, ?, 'ACTIVE', now())", id, id + "@example.com", hasher.hash(PASSWORD));
        return id;
    }

    private void refreshToken(UUID customerId) {
        jdbc.update("insert into refresh_token (id, customer_id, family_id, token_hash, expires_at) "
                + "values (?, ?, ?, ?, now() + interval '1 day')", UUID.randomUUID(), customerId, UUID.randomUUID(),
                UUID.randomUUID().toString());
    }

    private ResultActions changePassword(UUID id, String current) throws Exception {
        return mvc.perform(post("/v1/me/password").with(TestTenants.customer(id))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"" + current + "\",\"newPassword\":\"NewPass456\"}"));
    }

    private ResultActions closeAccount(UUID id, String password) throws Exception {
        return mvc.perform(post("/v1/me/delete").with(TestTenants.customer(id))
                .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"" + password + "\"}"));
    }

    private int failedAttempts(UUID id) {
        return jdbc.queryForObject("select failed_login_attempts from customer where id = ?", Integer.class, id);
    }

    @Test
    void repeatedWrongPasswordsLockPasswordChange() throws Exception {
        UUID id = customer();
        for (int attempt = 0; attempt < MAX_FAILURES; attempt++) {
            changePassword(id, "wrong").andExpect(status().isBadRequest());
        }

        changePassword(id, PASSWORD).andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("ACCOUNT_LOCKED"));
    }

    @Test
    void repeatedWrongPasswordsLockAccountClosure() throws Exception {
        UUID id = customer();
        for (int attempt = 0; attempt < MAX_FAILURES; attempt++) {
            closeAccount(id, "wrong").andExpect(status().isBadRequest());
        }

        closeAccount(id, PASSWORD).andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("ACCOUNT_LOCKED"));
    }

    @Test
    void failuresAreCountedAndAGoodPasswordResetsThem() throws Exception {
        UUID id = customer();
        changePassword(id, "wrong");
        changePassword(id, "wrong");
        assertThat(failedAttempts(id)).isEqualTo(2);

        closeAccount(id, "wrong");
        assertThat(failedAttempts(id)).isEqualTo(3);

        changePassword(id, PASSWORD).andExpect(status().isNoContent());
        assertThat(failedAttempts(id)).isZero();
    }

    @Test
    void parallelWrongGuessesAreAllCounted() throws Exception {
        UUID id = customer();
        int guesses = MAX_FAILURES - 1;
        ExecutorService pool = Executors.newFixedThreadPool(guesses);
        List<Callable<ResultActions>> tasks = IntStream.range(0, guesses)
                .<Callable<ResultActions>>mapToObj(i -> () -> changePassword(id, "wrong")).toList();
        pool.invokeAll(tasks);
        pool.shutdown();

        assertThat(failedAttempts(id)).isEqualTo(guesses);
    }

    @Test
    void changingThePasswordEndsEveryOpenSession() throws Exception {
        UUID id = customer();
        refreshToken(id);
        refreshToken(id);

        changePassword(id, PASSWORD).andExpect(status().isNoContent());

        assertThat(jdbc.queryForObject(
                "select count(*) from refresh_token where customer_id = ? and revoked_at is null", Integer.class, id))
                .isZero();
    }

    @Test
    void aRefusedPasswordChangeKeepsTheSessionsOpen() throws Exception {
        UUID id = customer();
        refreshToken(id);

        changePassword(id, "wrong").andExpect(status().isBadRequest());

        assertThat(jdbc.queryForObject(
                "select count(*) from refresh_token where customer_id = ? and revoked_at is null", Integer.class, id))
                .isEqualTo(1);
    }
}
