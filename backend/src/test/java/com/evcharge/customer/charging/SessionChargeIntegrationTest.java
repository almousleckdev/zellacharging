package com.evcharge.customer.charging;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.charging.application.SessionSettlement;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.charging.infrastructure.SessionChargeBackfill;
import com.evcharge.customer.shared.RuntimeSettings;
import com.evcharge.customer.shared.SettingKey;
import com.evcharge.customer.shared.TenantContext;
import com.evcharge.customer.wallet.WalletFunding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SessionChargeIntegrationTest extends PostgresTestBase {

    private static final Instant NOW = Instant.parse("2026-11-05T10:00:00Z");

    @Autowired
    SessionSettlement settlement;

    @Autowired
    WalletFunding funding;

    @Autowired
    RuntimeSettings runtime;

    @Autowired
    SessionChargeBackfill backfill;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void flatRates() {
        runtime.clear(SettingKey.VAT);
        runtime.clear(SettingKey.SERVICE_FEE);
    }

    private UUID customerWithWallet() {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, 'x', 'ACTIVE', now())", id, id + "@example.com");
        jdbc.update("insert into wallet (customer_id, currency, decimals, balance_minor, created_at) "
                + "values (?, 'XOF', 0, 100000, now())", id);
        return id;
    }

    private ChargingSession session(UUID customer, SessionState state, Long cost) {
        return new ChargingSession(UUID.randomUUID(), customer, "CP-1", 1, "Z-X", state, null, 77, NOW, NOW, null,
                cost, "XOF", 0, null, NOW, NOW);
    }

    private ChargingSession settled(SessionState state, Long cost, long reserve) {
        UUID customer = customerWithWallet();
        ChargingSession session = session(customer, state, cost);
        funding.reserve(customer, session.id(), reserve);
        settlement.settle(session);
        return session;
    }

    private Map<String, Object> row(UUID sessionId) {
        return jdbc.queryForMap("select * from session_charge where session_id = ?", sessionId);
    }

    private int rows(UUID sessionId) {
        return jdbc.queryForObject("select count(*) from session_charge where session_id = ?", Integer.class,
                sessionId);
    }

    @Test
    void completedSessionStoresTheBreakdownAtSettlement() {
        runtime.put(SettingKey.VAT, "18", null);
        runtime.put(SettingKey.SERVICE_FEE, "5", null);

        ChargingSession session = settled(SessionState.COMPLETED, 1000L, 5000);

        Map<String, Object> charge = row(session.id());
        assertThat(charge).containsEntry("state", "CHARGED").containsEntry("energy_cost_minor", 1000L)
                .containsEntry("vat_minor", 180L).containsEntry("service_fee_minor", 50L)
                .containsEntry("total_minor", 1230L).containsEntry("currency", "XOF")
                .containsEntry("estimated", false);
    }

    @Test
    void breakdownDoesNotChangeWhenTheFeeRatesChangeLater() {
        runtime.put(SettingKey.SERVICE_FEE, "5", null);
        ChargingSession session = settled(SessionState.COMPLETED, 1000L, 5000);
        Object before = row(session.id()).get("service_fee_minor");

        runtime.put(SettingKey.SERVICE_FEE, "20", null);

        assertThat(row(session.id()).get("service_fee_minor")).isEqualTo(before).isEqualTo(50L);
    }

    @Test
    void replayedSettlementKeepsOneRowAndTheFirstValues() {
        ChargingSession session = settled(SessionState.COMPLETED, 1000L, 5000);

        runtime.put(SettingKey.SERVICE_FEE, "20", null);
        settlement.settle(session);

        assertThat(rows(session.id())).isEqualTo(1);
        assertThat(row(session.id()).get("total_minor")).isEqualTo(1000L);
    }

    @Test
    void totalIsWhatTheWalletCapturedWhenCostExceedsTheBudget() {
        ChargingSession session = settled(SessionState.COMPLETED, 5000L, 1000);

        Map<String, Object> charge = row(session.id());
        assertThat(charge).containsEntry("state", "CHARGED").containsEntry("energy_cost_minor", 5000L)
                .containsEntry("total_minor", 1000L);
    }

    @Test
    void failedSessionStoresNotCharged() {
        ChargingSession session = settled(SessionState.FAILED, null, 5000);

        Map<String, Object> charge = row(session.id());
        assertThat(charge).containsEntry("state", "NOT_CHARGED").containsEntry("total_minor", null)
                .containsEntry("energy_cost_minor", null);
    }

    @Test
    void sessionWithoutCostStoresUnderReview() {
        ChargingSession session = settled(SessionState.COMPLETED, null, 5000);

        assertThat(row(session.id())).containsEntry("state", "UNDER_REVIEW").containsEntry("total_minor", null);
    }

    @Test
    void backfillMarksOldRowsEstimatedAndIsIdempotent() {
        runtime.put(SettingKey.SERVICE_FEE, "10", null);
        UUID customer = customerWithWallet();
        UUID completed = oldSession(customer, "COMPLETED", 2000L);
        UUID failed = oldSession(customer, "FAILED", null);
        UUID noCost = oldSession(customer, "ABANDONED", null);

        backfill.backfillAll();
        backfill.backfillAll();

        assertThat(row(completed)).containsEntry("state", "CHARGED").containsEntry("estimated", true)
                .containsEntry("energy_cost_minor", 2000L).containsEntry("service_fee_minor", 200L)
                .containsEntry("total_minor", 2200L);
        assertThat(row(failed)).containsEntry("state", "NOT_CHARGED").containsEntry("estimated", true);
        assertThat(row(noCost)).containsEntry("state", "UNDER_REVIEW");
        assertThat(rows(completed)).isEqualTo(1);
    }

    @Test
    void backfillNeverTouchesAnExistingRow() {
        ChargingSession session = settled(SessionState.COMPLETED, 1000L, 5000);
        jdbc.update("update charging_session set state = 'COMPLETED', cost_minor = 1000 where id = ?", session.id());

        backfill.backfillAll();

        assertThat(row(session.id())).containsEntry("estimated", false);
    }

    @Test
    void backfillSkipsSessionsWhoseHoldIsStillOpenAndSettlementWritesThemLater() {
        UUID customer = customerWithWallet();
        UUID id = oldSession(customer, "COMPLETED", 5000L);
        funding.reserve(customer, id, 2000);

        backfill.backfillAll();
        assertThat(rows(id)).isZero();

        ChargingSession session = new ChargingSession(id, customer, "CP-OLD", 1, "Z-OLD", SessionState.COMPLETED,
                null, 77, NOW, NOW, null, 5000L, "XOF", 0, null, NOW, NOW);
        settlement.settle(session);

        assertThat(row(id)).containsEntry("state", "CHARGED").containsEntry("total_minor", 2000L)
                .containsEntry("estimated", false);
    }

    @Test
    void backfillUsesTheSameStateRulesAsSettlementWhenTheHoldCapturedNothing() {
        UUID customer = customerWithWallet();
        UUID id = oldSession(customer, "COMPLETED", 5000L);
        funding.reserve(customer, id, 2000);
        funding.settle(id, 5000L, "EUR");

        backfill.backfillAll();

        assertThat(row(id)).containsEntry("state", "UNDER_REVIEW").containsEntry("total_minor", null)
                .containsEntry("estimated", true);
    }

    @Test
    void rowsAreInvisibleToAnotherCountry() {
        ChargingSession session = settled(SessionState.COMPLETED, 1000L, 5000);

        Integer visible = TenantContext.callAs("ML", () -> jdbc.queryForObject(
                "select count(*) from session_charge where session_id = ?", Integer.class, session.id()));

        assertThat(visible).isZero();
        assertThat(rows(session.id())).isEqualTo(1);
    }

    private UUID oldSession(UUID customer, String state, Long cost) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into charging_session (id, customer_id, station_id, connector_id, id_tag, state, "
                + "transaction_id, cost_minor, currency, currency_decimals, created_at, updated_at) "
                + "values (?, ?, 'CP-OLD', 1, 'Z-OLD', ?, ?, ?, 'XOF', 0, now(), now())", id, customer, state,
                (int) (Math.random() * 1_000_000) + 1, cost);
        return id;
    }
}
