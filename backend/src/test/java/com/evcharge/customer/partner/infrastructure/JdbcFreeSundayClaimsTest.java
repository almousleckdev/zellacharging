package com.evcharge.customer.partner.infrastructure;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.partner.FreeSundayClaims;
import com.evcharge.customer.partner.PartnerFixtures;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JdbcFreeSundayClaimsTest extends PostgresTestBase {

    private static final LocalDate SUNDAY = LocalDate.of(2026, 11, 22);

    @Autowired
    FreeSundayClaims claims;

    @Autowired
    JdbcTemplate jdbc;

    private UUID customer;
    private Instant inOneHour;

    @BeforeEach
    void prepare() {
        customer = new PartnerFixtures(jdbc).customer(TENANT, "A", "B", UUID.randomUUID() + "@example.com",
                null, "ACTIVE");
        inOneHour = Instant.now().plus(1, ChronoUnit.HOURS);
    }

    private boolean claim(UUID session) {
        return claims.claim(customer, SUNDAY, session, UUID.randomUUID(), inOneHour);
    }

    @Test
    void firstClaimIsTaken() {
        assertThat(claim(UUID.randomUUID())).isTrue();
    }

    @Test
    void secondClaimForTheSameSundayIsRefused() {
        assertThat(claim(UUID.randomUUID())).isTrue();
        assertThat(claim(UUID.randomUUID())).isFalse();
    }

    @Test
    void anotherSundayCanBeClaimed() {
        assertThat(claim(UUID.randomUUID())).isTrue();
        assertThat(claims.claim(customer, SUNDAY.plusDays(7), UUID.randomUUID(), UUID.randomUUID(), inOneHour))
                .isTrue();
    }

    @Test
    void fiftyParallelClaimsForOneCustomerAndSundayYieldExactlyOne() throws Exception {
        int attempts = 50;
        ExecutorService pool = Executors.newFixedThreadPool(attempts);
        CountDownLatch ready = new CountDownLatch(attempts);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();
        for (int i = 0; i < attempts; i++) {
            results.add(pool.submit(() -> {
                ready.countDown();
                start.await();
                return TenantContext.callAs(TENANT, () -> claim(UUID.randomUUID()));
            }));
        }
        ready.await();
        start.countDown();
        int taken = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) {
                taken++;
            }
        }
        pool.shutdown();
        assertThat(taken).isEqualTo(1);
    }

    @Test
    void isClaimedReflectsOnlyOpenClaims() {
        UUID session = UUID.randomUUID();
        assertThat(claims.isClaimed(UUID.randomUUID(), customer, SUNDAY)).isFalse();
        claim(session);
        assertThat(claims.isClaimed(UUID.randomUUID(), customer, SUNDAY)).isTrue();
        claims.release(session);
        assertThat(claims.isClaimed(UUID.randomUUID(), customer, SUNDAY)).isFalse();
    }

    @Test
    void releasedClaimCanBeTakenAgain() {
        UUID first = UUID.randomUUID();
        claim(first);
        claims.release(first);
        assertThat(claim(UUID.randomUUID())).isTrue();
    }

    @Test
    void claimsAreInvisibleToAnotherCountry() {
        UUID session = UUID.randomUUID();
        claim(session);
        TenantContext.runAs("ML", () -> {
            assertThat(claims.agreementOf(session)).isEmpty();
            assertThat(claims.sessionsPastWindow(Instant.now(), Instant.now().plus(1, ChronoUnit.DAYS)))
                    .doesNotContain(session);
        });
    }

    @Test
    void agreementOfReturnsTheClaimedAgreementAndNothingOnceReleased() {
        UUID session = UUID.randomUUID();
        UUID agreement = UUID.randomUUID();
        claims.claim(customer, SUNDAY, session, agreement, inOneHour);
        assertThat(claims.agreementOf(session)).contains(agreement);
        claims.release(session);
        assertThat(claims.agreementOf(session)).isEmpty();
        assertThat(claims.agreementOf(UUID.randomUUID())).isEmpty();
    }

    @Test
    void sessionsPastWindowReturnsOnlyClaimedSessionsWhoseWindowHasEnded() {
        UUID past = UUID.randomUUID();
        UUID released = UUID.randomUUID();
        UUID future = UUID.randomUUID();
        Instant ended = Instant.now().minus(1, ChronoUnit.MINUTES);
        claims.claim(customer, SUNDAY, past, UUID.randomUUID(), ended);
        claims.claim(customer, SUNDAY.plusDays(7), released, UUID.randomUUID(), ended);
        claims.release(released);
        claims.claim(customer, SUNDAY.plusDays(14), future, UUID.randomUUID(), inOneHour);
        assertThat(claims.sessionsPastWindow(Instant.now().minus(1, ChronoUnit.DAYS), Instant.now()))
                .contains(past).doesNotContain(released, future);
    }

    @Test
    void theWindowEndOfAClaimedSessionIsReadBack() {
        UUID session = UUID.randomUUID();
        assertThat(claim(session)).isTrue();

        assertThat(claims.windowEnd(session)).hasValueSatisfying(end ->
                assertThat(end).isCloseTo(inOneHour, org.assertj.core.api.Assertions.within(1, ChronoUnit.SECONDS)));
    }

    @Test
    void aReleasedClaimOrAnUnknownSessionHasNoWindowEnd() {
        UUID session = UUID.randomUUID();
        assertThat(claim(session)).isTrue();
        claims.release(session);

        assertThat(claims.windowEnd(session)).isEmpty();
        assertThat(claims.windowEnd(UUID.randomUUID())).isEmpty();
    }

    @Test
    void sessionsEndingInsideTheRangeAreListedAndOthersAreNot() {
        UUID soon = UUID.randomUUID();
        assertThat(claim(soon)).isTrue();
        Instant now = inOneHour.minus(20, ChronoUnit.MINUTES);

        assertThat(claims.sessionsEndingBetween(now, now.plus(30, ChronoUnit.MINUTES))).contains(soon);
        assertThat(claims.sessionsEndingBetween(now.minus(2, ChronoUnit.HOURS), now.minus(1, ChronoUnit.HOURS)))
                .doesNotContain(soon);
        assertThat(claims.sessionsEndingBetween(inOneHour, inOneHour.plus(1, ChronoUnit.HOURS))).doesNotContain(soon);
    }

    @Test
    void aReleasedClaimIsNeverListedAsEnding() {
        UUID session = UUID.randomUUID();
        assertThat(claim(session)).isTrue();
        claims.release(session);

        assertThat(claims.sessionsEndingBetween(inOneHour.minus(30, ChronoUnit.MINUTES), inOneHour.plus(1, ChronoUnit.MINUTES)))
                .doesNotContain(session);
    }
}
