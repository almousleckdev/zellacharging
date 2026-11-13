package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.SessionTelemetry;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.LastReading;
import com.evcharge.customer.charging.domain.Metering;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
public class MeteringProjection {

    private static final long LONGEST_SECONDS = 120;
    private static final BigDecimal SECONDS_PER_HOUR = BigDecimal.valueOf(3600);
    private static final BigDecimal ENOUGH_ENERGY = new BigDecimal("0.2");
    private static final BigDecimal FULL = BigDecimal.valueOf(100);

    private final SessionTelemetry telemetry;
    private final Clock clock;

    public MeteringProjection(SessionTelemetry telemetry, Clock clock) {
        this.telemetry = telemetry;
        this.clock = clock;
    }

    public Optional<Metering> of(ChargingSession session, Optional<Metering> reading) {
        return reading.map(measured -> project(session, measured));
    }

    private Metering project(ChargingSession session, Metering measured) {
        if (measured.totalKwh() == null || measured.powerKw() == null || measured.powerKw().signum() <= 0) {
            return measured;
        }
        long seconds = secondsSinceMeasured(session);
        if (seconds <= 0) {
            return measured;
        }
        BigDecimal extra = measured.powerKw().multiply(BigDecimal.valueOf(seconds))
                .divide(SECONDS_PER_HOUR, 3, RoundingMode.HALF_DOWN);
        Long duration = measured.durationSeconds() == null ? null : measured.durationSeconds() + seconds;
        return new Metering(measured.totalKwh().add(extra), measured.powerKw(), battery(session, measured, extra),
                duration, measured.voltage(), measured.currentAmpere(), measured.sampledAt());
    }

    private long secondsSinceMeasured(ChargingSession session) {
        Optional<Instant> seen = telemetry.readingAtOf(session.id());
        if (seen.isEmpty()) {
            return 0;
        }
        long elapsed = Duration.between(seen.get(), clock.instant()).toSeconds();
        return Math.max(0, Math.min(elapsed, LONGEST_SECONDS));
    }

    private BigDecimal battery(ChargingSession session, Metering measured, BigDecimal extraKwh) {
        LastReading last = telemetry.lastReadingOf(session.id());
        BigDecimal now = measured.stateOfChargePercent();
        BigDecimal start = last.startBatteryPercent();
        if (now == null || start == null || measured.totalKwh().compareTo(ENOUGH_ENERGY) < 0) {
            return now;
        }
        BigDecimal percentPerKwh = now.subtract(start).divide(measured.totalKwh(), 6, RoundingMode.HALF_UP);
        BigDecimal projected = now.add(percentPerKwh.multiply(extraKwh)).setScale(1, RoundingMode.HALF_DOWN);
        return projected.min(FULL);
    }
}
