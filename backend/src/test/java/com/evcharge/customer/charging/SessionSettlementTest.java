package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.ChargeAnnouncements;
import com.evcharge.customer.charging.application.ChargingMetrics;
import com.evcharge.customer.charging.application.FeeCalculator;
import com.evcharge.customer.charging.application.FeeSettings;
import com.evcharge.customer.charging.application.SessionChargeRecorder;
import com.evcharge.customer.charging.application.SessionSettlement;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.wallet.WalletFunding;
import com.evcharge.customer.shared.FreeSundayAccess;
import com.evcharge.customer.shared.Money;
import com.evcharge.customer.shared.Notifier;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessionSettlementTest {

    private static final Instant NOW = Instant.parse("2026-10-16T12:00:00Z");

    private final WalletFunding funding = mock(WalletFunding.class);
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final ChargingMetrics metrics = new ChargingMetrics(registry);
    private final Notifier notifier = mock(Notifier.class);
    private final FreeSundayAccess freeSunday = mock(FreeSundayAccess.class);
    private final ChargeAnnouncements announcements = mock(ChargeAnnouncements.class);

    @BeforeEach
    void reservation() {
        when(funding.reserved(any())).thenReturn(new Money(5000, "XAF", 0));
    }

    private ChargingSession completed(Long costMinor) {
        return new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1, "Z-X", SessionState.COMPLETED,
                null, 7, NOW, NOW, null, costMinor, "XAF", 0, null, NOW, NOW);
    }

    @Test
    void chargesTheElectricFeePlusVatAndServiceFee() {
        SessionSettlement settlement = new SessionSettlement(funding,
                new FeeCalculator(new FeeSettings(new BigDecimal("18"), new BigDecimal("5"))), metrics, notifier, mock(SessionChargeRecorder.class), freeSunday, announcements);
        ChargingSession session = completed(1000L);

        settlement.settle(session);

        verify(funding).settle(session.id(), 1230L, "XAF");
    }

    @Test
    void settlesWithoutACostWhenNoneIsKnown() {
        SessionSettlement settlement = new SessionSettlement(funding, new FeeCalculator(new FeeSettings(null, null)), metrics, notifier, mock(SessionChargeRecorder.class), freeSunday, announcements);
        ChargingSession session = completed(null);

        settlement.settle(session);

        verify(funding).settle(session.id(), null, "XAF");
    }

    @Test
    void aSessionWithoutACostIsFlaggedForReviewAndChargesNothing() {
        SessionSettlement settlement = new SessionSettlement(funding, new FeeCalculator(new FeeSettings(null, null)), metrics, notifier, mock(SessionChargeRecorder.class), freeSunday, announcements);

        settlement.settle(completed(null));

        assertThat(registry.counter("zella.charging.settlement.review").count()).isEqualTo(1.0);
    }

    @Test
    void aCostAboveTheAuthorizedAmountIsRecordedAsOvershootAndTheAuthorizedAmountIsStillTheCeiling() {
        SessionSettlement settlement = new SessionSettlement(funding, new FeeCalculator(new FeeSettings(null, null)), metrics, notifier, mock(SessionChargeRecorder.class), freeSunday, announcements);
        ChargingSession session = completed(6200L);

        settlement.settle(session);

        verify(funding).settle(session.id(), 6200L, "XAF");
        assertThat(registry.summary("zella.charging.overshoot.minor").totalAmount()).isEqualTo(1200.0);
    }

    @Test
    void onlyANeverStartedOrEnergylessSessionGivesItsFreeSundayBack() {
        SessionSettlement settlement = new SessionSettlement(funding, new FeeCalculator(new FeeSettings(null, null)), metrics, notifier, mock(SessionChargeRecorder.class), freeSunday, announcements);
        ChargingSession charged = completed(1000L).withMeasuredEnergy(new BigDecimal("2.5"));
        ChargingSession failed = ChargingSession.requested(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1, "Z-X", NOW)
                .withState(SessionState.FAILED, "CHARGER_REFUSED", NOW);
        ChargingSession running = completed(null).withState(SessionState.ACTIVE, null, NOW);

        settlement.settle(charged);
        settlement.settle(failed);
        settlement.settle(running);

        verify(freeSunday, never()).release(charged.id());
        verify(freeSunday).release(failed.id());
        verify(freeSunday, never()).release(running.id());
    }

    @Test
    void aStartedSessionWithUnknownEnergyKeepsItsFreeSunday() {
        SessionSettlement settlement = new SessionSettlement(funding, new FeeCalculator(new FeeSettings(null, null)), metrics, notifier, mock(SessionChargeRecorder.class), freeSunday, announcements);
        ChargingSession abandoned = completed(null).withState(SessionState.ABANDONED, "CHARGER_OFFLINE", NOW);
        ChargingSession stoppedEarly = completed(null).withState(SessionState.FAILED, "CHARGER_STOPPED_EARLY", NOW);

        settlement.settle(abandoned);
        settlement.settle(stoppedEarly);

        verify(freeSunday, never()).release(abandoned.id());
        verify(freeSunday, never()).release(stoppedEarly.id());
    }

    @Test
    void aChargeThatBecomesActiveIsAnnouncedThroughTheAnnouncements() {
        SessionSettlement settlement = new SessionSettlement(funding, new FeeCalculator(new FeeSettings(null, null)),
                metrics, notifier, mock(SessionChargeRecorder.class), freeSunday, announcements);
        ChargingSession before = new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1, "Z-X",
                SessionState.START_ACCEPTED, null, 7, NOW, null, null, null, "XAF", 0, null, NOW, NOW);
        ChargingSession after = before.withState(SessionState.ACTIVE, null, NOW);

        settlement.onChange(before, after);

        verify(announcements).started(after);
    }

    @Test
    void noAnnouncementIsMadeWhenTheSessionWasAlreadyActive() {
        SessionSettlement settlement = new SessionSettlement(funding, new FeeCalculator(new FeeSettings(null, null)),
                metrics, notifier, mock(SessionChargeRecorder.class), freeSunday, announcements);
        ChargingSession active = new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1, "Z-X",
                SessionState.ACTIVE, null, 7, NOW, null, null, null, "XAF", 0, null, NOW, NOW);

        settlement.onChange(active, active);

        verify(announcements, never()).started(any());
    }
}
