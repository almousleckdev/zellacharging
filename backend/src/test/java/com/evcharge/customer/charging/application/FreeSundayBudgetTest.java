package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.Metering;
import com.evcharge.customer.wallet.WalletFunding;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.evcharge.customer.charging.application.FreeSundayFixtures.started;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.when;

class FreeSundayBudgetTest extends FreeSundayTestBase {

    private static final long CLIENT_AMOUNT = 200;

    @Autowired
    WalletFunding funding;

    @Autowired
    SessionSettings settings;

    @Autowired
    SessionTariff tariff;

    @Autowired
    FeeCalculator fees;

    @Autowired
    ChargingMetrics metrics;

    @Autowired
    Clock clock;

    private long maximum() {
        return funding.startLimits().maximumMinor();
    }

    private long reservedFor(ChargingSession session) {
        return jdbc.queryForObject("select reserved_minor from funding_hold where session_id = ?", Long.class,
                session.id());
    }

    private boolean guardStops(ChargingSession session) {
        MeteringService metering = mock(MeteringService.class);
        StopCommander stopper = mock(StopCommander.class);
        when(metering.of(any())).thenReturn(Optional.of(new Metering(new BigDecimal("10.0"), null, null, 10L, null,
                null, Instant.now())));
        new FundsGuard(funding, metering, stopper, settings, tariff, fees, metrics, clock).enforce(session);
        return !mockingDetails(stopper).getInvocations().isEmpty();
    }

    @Test
    void aFreeStartReservesTheServerMaximumWhateverTheClientSent() {
        ChargingSession session = service.start(driver(0), fixtures.station(), 1, CLIENT_AMOUNT, null);

        assertThat(fixtures.waived(session.id())).isTrue();
        assertThat(reservedFor(session)).isEqualTo(maximum());
        assertThat(jdbc.queryForObject("select detail from audit_record where action = 'SESSION_START_REQUESTED' "
                + "and subject = ?", String.class, session.id().toString())).endsWith("budget=" + maximum());
    }

    @Test
    void aPaidStartKeepsTheClientAmount() {
        UUID customer = fixtures.linkedCustomerWith(10000);
        ChargingSession session = service.start(customer, fixtures.station(), 1, CLIENT_AMOUNT, null);

        assertThat(fixtures.waived(session.id())).isFalse();
        assertThat(reservedFor(session)).isEqualTo(CLIENT_AMOUNT);
    }

    @Test
    void theGuardDoesNotStopAFreeSessionPastTheClientAmount() {
        ChargingSession free = started(service.start(driver(0), fixtures.station(), 1, CLIENT_AMOUNT, null));
        ChargingSession paid = started(service.start(fixtures.linkedCustomerWith(10000), fixtures.station(), 1,
                CLIENT_AMOUNT, null));

        assertThat(guardStops(free)).isFalse();
        assertThat(guardStops(paid)).isTrue();
    }
}
