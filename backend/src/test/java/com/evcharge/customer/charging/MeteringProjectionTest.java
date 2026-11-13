package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.MeteringProjection;
import com.evcharge.customer.charging.application.port.SessionTelemetry;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.LastReading;
import com.evcharge.customer.charging.domain.Metering;
import com.evcharge.customer.charging.domain.SessionState;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MeteringProjectionTest {

    private static final Instant NOW = Instant.parse("2026-11-13T12:00:00Z");

    private final SessionTelemetry telemetry = mock(SessionTelemetry.class);
    private final MeteringProjection projection = new MeteringProjection(telemetry,
            Clock.fixed(NOW, ZoneOffset.UTC));

    private ChargingSession session() {
        return new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1, "Z-X", SessionState.ACTIVE,
                null, 7, NOW.minusSeconds(300), null, null, null, "XAF", 0, null, NOW.minusSeconds(300), NOW);
    }

    private static Metering measured(String kwh, String power, String soc) {
        return new Metering(new BigDecimal(kwh), new BigDecimal(power), new BigDecimal(soc), 100L, null, null,
                NOW.minusSeconds(60));
    }

    private void measuredSecondsAgo(ChargingSession session, long seconds, String startSoc) {
        when(telemetry.readingAtOf(session.id())).thenReturn(Optional.of(NOW.minusSeconds(seconds)));
        when(telemetry.lastReadingOf(session.id()))
                .thenReturn(new LastReading(new BigDecimal(startSoc), null, null, null));
    }

    @Test
    void energyAndBatteryAdvanceWithThePowerSinceTheLastReport() {
        ChargingSession session = session();
        measuredSecondsAgo(session, 60, "20");

        Metering now = projection.of(session, Optional.of(measured("2.000", "36", "30"))).orElseThrow();

        assertThat(now.totalKwh()).isEqualByComparingTo("2.600");
        assertThat(now.stateOfChargePercent()).isEqualByComparingTo("33.0");
        assertThat(now.durationSeconds()).isEqualTo(160);
    }

    @Test
    void theBatteryNeverGoesPastFull() {
        ChargingSession session = session();
        measuredSecondsAgo(session, 120, "20");

        Metering now = projection.of(session, Optional.of(measured("2.000", "36", "99"))).orElseThrow();

        assertThat(now.stateOfChargePercent()).isEqualByComparingTo("100");
    }

    @Test
    void aReportOlderThanTwoMinutesIsNotProjectedFurther() {
        ChargingSession session = session();
        measuredSecondsAgo(session, 900, "20");

        Metering now = projection.of(session, Optional.of(measured("2.000", "36", "30"))).orElseThrow();

        assertThat(now.totalKwh()).isEqualByComparingTo("3.200");
    }

    @Test
    void withoutPowerOrArrivalTimeTheReportIsShownAsMeasured() {
        ChargingSession session = session();
        when(telemetry.readingAtOf(session.id())).thenReturn(Optional.empty());
        Metering report = measured("2.000", "36", "30");

        assertThat(projection.of(session, Optional.of(report))).contains(report);
        assertThat(projection.of(session, Optional.empty())).isEmpty();
    }
}
