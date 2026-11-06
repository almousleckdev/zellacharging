package com.evcharge.customer.admin;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.application.AdminRefreshService;
import com.evcharge.customer.admin.application.AdminTokens;
import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminRefreshTokenRepository;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.application.port.AdminTokenIssuer;
import com.evcharge.customer.admin.domain.AdminRefreshToken;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.Sha256;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AdminRefreshIntegrationTest extends PostgresTestBase {

    @Autowired
    AdminRefreshService refresh;
    @Autowired
    AdminRefreshTokenRepository refreshTokens;
    @Autowired
    AdminRepository admins;
    @Autowired
    AdminPasswordHasher passwords;
    @Autowired
    AdminTokenIssuer tokens;
    @Autowired
    JdbcTemplate jdbc;

    private UUID family;
    private String raw;

    @BeforeEach
    void seed() {
        TenantContext.clear();
        AdminFixtures.Created admin = new AdminFixtures(admins, passwords, tokens).create(AdminRole.SUPER_ADMIN,
                Set.of());
        family = UUID.randomUUID();
        raw = "raw-" + UUID.randomUUID();
        refreshTokens.save(new AdminRefreshToken(UUID.randomUUID(), admin.id(), family, Sha256.hex(raw),
                Instant.now().plusSeconds(3600), null, null));
    }

    private void usedLongAgo() {
        jdbc.update("update admin_refresh_token set used_at = ? where token_hash = ?",
                Timestamp.from(Instant.now().minusSeconds(120)), Sha256.hex(raw));
    }

    @Test
    void aTokenUsedAgainRightAfterTheFirstRefreshStillRenewsTheSession() {
        AdminTokens first = refresh.refresh(raw);
        AdminTokens second = refresh.refresh(raw);

        assertThat(second.accessToken()).isNotBlank();
        assertThat(refresh.refresh(first.refreshToken()).accessToken()).isNotBlank();
    }

    @Test
    void parallelRefreshesWithTheSameTokenAllSucceed() throws Exception {
        int callers = 8;
        ExecutorService pool = Executors.newFixedThreadPool(callers);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<AdminTokens>> results = new ArrayList<>();
        for (int i = 0; i < callers; i++) {
            results.add(pool.submit(() -> {
                start.await();
                return refresh.refresh(raw);
            }));
        }
        start.countDown();
        for (Future<AdminTokens> result : results) {
            assertThat(result.get().refreshToken()).isNotBlank();
        }
        pool.shutdown();
    }

    @Test
    void aReplayLongAfterTheFirstUseIsTheftAndEndsTheWholeSession() {
        AdminTokens first = refresh.refresh(raw);
        usedLongAgo();

        assertThatThrownBy(() -> refresh.refresh(raw)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> refresh.refresh(first.refreshToken())).isInstanceOf(ApiException.class);
    }

    @Test
    void aRevokedTokenIsRefusedAtOnceEvenRightAfterItsUse() {
        refresh.refresh(raw);
        refreshTokens.revokeFamily(family, Instant.now());

        assertThatThrownBy(() -> refresh.refresh(raw)).isInstanceOf(ApiException.class);
    }
}
