package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.AuditTrail;
import com.evcharge.customer.shared.FreeSundayWindows;
import com.evcharge.customer.wallet.WalletFunding;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.evcharge.customer.charging.application.FreeSundayFixtures.started;
import static org.assertj.core.api.Assertions.assertThat;

class FreeWindowStopIntegrationTest extends FreeSundayTestBase {

    private static final Instant BASE_END = Instant.parse("2030-03-03T21:00:00Z");
    private static final AtomicInteger WEEKS = new AtomicInteger();

    @Autowired
    FreeSundayWindows windows;

    @Autowired
    StopCommander stopCommander;

    @Autowired
    AuditTrail audit;

    @Autowired
    SessionSweeper sweeper;

    @Autowired
    ChargeAnnouncements announcements;

    @Autowired
    FreeWindowReminder reminder;

    @Autowired
    WalletFunding funding;

    private final Instant windowEnd = BASE_END.plus(Duration.ofDays(7L * WEEKS.incrementAndGet()));

    private FreeWindowStopper stopperAt(Instant now) {
        return new FreeWindowStopper(windows, sessions, funding, stopCommander, announcements, audit,
                Clock.fixed(now, ZoneOffset.UTC));
    }

    private ChargingSession charging(UUID customer, Instant ends) {
        String station = fixtures.station();
        OCPP.route("POST", stopPath(station), 200, "{}");
        ChargingSession session = started(start(customer, station, null));
        sessions.update(session);
        jdbc.update("update free_sunday_claim set window_ends_at = ? where session_id = ?", Timestamp.from(ends),
                session.id());
        return session;
    }

    private static String stopPath(String station) {
        return "/api/v1/charge-points/" + station + "/connectors/1/stop";
    }

    private int stopsSentTo(ChargingSession session) {
        return OCPP.hits("POST", stopPath(session.stationId()));
    }

    private SessionState stateOf(ChargingSession session) {
        return sessions.findById(session.id()).orElseThrow().state();
    }

    @Test
    void doesNotStopBeforeTheEnd() {
        ChargingSession session = charging(driver(0), windowEnd);

        stopperAt(windowEnd.minusSeconds(1)).stopExpired();
        assertThat(stopsSentTo(session)).isZero();
        assertThat(stateOf(session)).isEqualTo(SessionState.ACTIVE);

        stopperAt(windowEnd).stopExpired();
        assertThat(stopsSentTo(session)).isEqualTo(1);
        assertThat(stateOf(session)).isEqualTo(SessionState.STOPPING);
    }

    @Test
    void neverStopsAPaidSession() {
        UUID customer = fixtures.linkedCustomerWith(10000);
        ChargingSession paid = charging(customer, windowEnd);
        jdbc.update("insert into free_sunday_claim (id, customer_id, sunday_date, session_id, agreement_id, "
                + "window_ends_at) values (?, ?, ?, ?, ?, ?)", UUID.randomUUID(), customer,
                FreeSundayFixtures.SUNDAY, paid.id(), UUID.randomUUID(), Timestamp.from(windowEnd));

        stopperAt(windowEnd.plusSeconds(60)).stopExpired();

        assertThat(fixtures.claimStateOf(paid.id())).isEqualTo("CLAIMED");
        assertThat(fixtures.waived(paid.id())).isFalse();
        assertThat(stopsSentTo(paid)).isZero();
        assertThat(stateOf(paid)).isEqualTo(SessionState.ACTIVE);
    }

    @Test
    void finishedSessionsOfEarlierSundaysAreNeverStopped() {
        ChargingSession earlier = charging(driver(0), windowEnd.minus(Duration.ofDays(7)));
        close(earlier, SessionState.COMPLETED, new BigDecimal("4.0"));

        stopperAt(windowEnd).stopExpired();

        assertThat(stopsSentTo(earlier)).isZero();
        assertThat(fixtures.claimStateOf(earlier.id())).isEqualTo("CLAIMED");
    }

    @Test
    void aWindowStoppedSessionEndsCompletedFreeAndKeepsItsSunday() {
        UUID customer = driver(5000);
        ChargingSession session = charging(customer, windowEnd);
        FreeSundayUpkeep upkeep = new FreeSundayUpkeep(stopperAt(windowEnd.plusSeconds(5)), reminder, windows, audit,
                Clock.fixed(windowEnd.plusSeconds(5), ZoneOffset.UTC));

        upkeep.tend();

        assertThat(stopsSentTo(session)).isEqualTo(1);
        OCPP.route("GET", "/api/v1/sessions/" + session.transactionId(), 200, "{\"transactionId\":"
                + session.transactionId() + ",\"deviceId\":\"" + session.stationId() + "\",\"connectorId\":1,"
                + "\"status\":\"COMPLETED\",\"startedAt\":\"" + session.startedAt() + "\",\"stoppedAt\":\""
                + Instant.now() + "\",\"energyKwh\":3.5,\"costMinor\":1750,\"currency\":\"XOF\","
                + "\"currencyDecimals\":0,\"stopReason\":\"Remote\"}");

        sweeper.sweep();

        ChargingSession ended = sessions.findById(session.id()).orElseThrow();
        assertThat(ended.state()).isEqualTo(SessionState.COMPLETED);
        assertThat(ended.stopReason()).isEqualTo(FreeWindowStopper.REASON);
        assertThat(ended.energyKwh()).isEqualByComparingTo("3.5");
        assertThat(fixtures.holdState(session.id())).isEqualTo("RELEASED");
        assertThat(jdbc.queryForObject("select captured_minor from funding_hold where session_id = ?", Long.class,
                session.id())).isZero();
        assertThat(fixtures.balance(customer)).isEqualTo(5000);
        assertThat(fixtures.claimStateOf(session.id())).isEqualTo("CLAIMED");
        assertThat(jdbc.queryForObject("select count(*) from audit_record where action = "
                + "'FREE_SUNDAY_WINDOW_STOPPED' and subject = ?", Integer.class, session.id().toString())).isOne();
    }
}
