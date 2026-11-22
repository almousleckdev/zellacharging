package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.evcharge.customer.charging.application.FreeSundayFixtures.eligible;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class FreeSundayParallelStartTest extends FreeSundayTestBase {

    private static final int ROUNDS = 5;

    @Test
    void parallelStartsNeverGiveTwoFreeSessions() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            for (int round = 0; round < ROUNDS; round++) {
                race(pool);
            }
        } finally {
            pool.shutdownNow();
        }
    }

    private void race(ExecutorService pool) throws Exception {
        UUID customer = fixtures.linkedCustomerWith(10000);
        CyclicBarrier bothAsking = new CyclicBarrier(2);
        when(ndzela.check(customer)).thenAnswer(invocation -> {
            bothAsking.await(10, TimeUnit.SECONDS);
            return eligible();
        });
        List<CompletableFuture<ChargingSession>> attempts = List.of(attempt(pool, customer),
                attempt(pool, customer));
        long started = attempts.stream().map(this::outcome).filter(s -> s != null).count();

        List<UUID> live = fixtures.liveSessions(customer);
        List<String> claims = fixtures.claimStates(customer);
        assertThat(started).isOne();
        assertThat(live).hasSize(1);
        assertThat(claims).hasSize(1);
        assertThat(fixtures.waivedHolds(customer)).isLessThanOrEqualTo(1);
        boolean free = fixtures.waived(live.getFirst());
        assertThat(claims.getFirst()).isEqualTo(free ? "CLAIMED" : "RELEASED");
        assertThat(fixtures.reserved(customer)).isEqualTo(free ? 0 : 2000);
    }

    private CompletableFuture<ChargingSession> attempt(ExecutorService pool, UUID customer) {
        String station = fixtures.station();
        String key = UUID.randomUUID().toString();
        return CompletableFuture.supplyAsync(() -> TenantContext.callAs(TENANT, () -> start(customer, station, key)),
                pool);
    }

    private ChargingSession outcome(CompletableFuture<ChargingSession> attempt) {
        try {
            return attempt.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            return null;
        }
    }
}
