package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.SessionTelemetry;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.LastReading;
import com.evcharge.customer.charging.domain.SessionProgress;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.Money;
import com.evcharge.customer.wallet.WalletFunding;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class ConnectorWait {

    private static final BigDecimal MINIMUM_POWER_KW = new BigDecimal("0.5");
    private static final BigDecimal ENOUGH_ENERGY = new BigDecimal("0.2");
    private static final BigDecimal FULL = BigDecimal.valueOf(100);
    private static final int LONGEST_MINUTES = 600;

    private final SessionTelemetry telemetry;
    private final SessionSettings settings;
    private final SessionCostEstimator estimator;
    private final FeeCalculator fees;
    private final WalletFunding funding;
    private final Clock clock;

    public ConnectorWait(SessionTelemetry telemetry, SessionSettings settings, SessionCostEstimator estimator,
                         FeeCalculator fees, WalletFunding funding, Clock clock) {
        this.telemetry = telemetry;
        this.settings = settings;
        this.estimator = estimator;
        this.fees = fees;
        this.funding = funding;
        this.clock = clock;
    }

    public Optional<Integer> minutesLeft(ChargingSession session) {
        return switch (session.state()) {
            case REQUESTED, START_ACCEPTED -> untilPlugInTimeout(session);
            case STOPPING -> Optional.of(1);
            case ACTIVE -> whileCharging(session);
            default -> Optional.empty();
        };
    }

    private Optional<Integer> untilPlugInTimeout(ChargingSession session) {
        Instant deadline = session.createdAt().plus(settings.plugInTimeout());
        return Optional.of(minutes(Duration.between(clock.instant(), deadline).toSeconds()));
    }

    private Optional<Integer> whileCharging(ChargingSession session) {
        LastReading last = telemetry.lastReadingOf(session.id());
        BigDecimal power = last.powerKw();
        SessionProgress progress = telemetry.progressOf(session.id());
        if (power == null || power.compareTo(MINIMUM_POWER_KW) < 0 || progress.energyKwh() == null) {
            return Optional.empty();
        }
        return Stream.of(kwhToFull(last, progress.energyKwh()), kwhToBudget(session, progress.energyKwh()))
                .flatMap(Optional::stream).min(BigDecimal::compareTo)
                .map(kwh -> minutes(kwh.multiply(BigDecimal.valueOf(3600)).divide(power, 0, RoundingMode.CEILING)
                        .longValue()));
    }

    private Optional<BigDecimal> kwhToFull(LastReading last, BigDecimal energy) {
        BigDecimal now = last.batteryPercent();
        BigDecimal start = last.startBatteryPercent();
        if (now == null || start == null || energy.compareTo(ENOUGH_ENERGY) < 0 || now.compareTo(start) <= 0) {
            return Optional.empty();
        }
        BigDecimal percentPerKwh = now.subtract(start).divide(energy, 6, RoundingMode.HALF_UP);
        return Optional.of(FULL.subtract(now).max(BigDecimal.ZERO).divide(percentPerKwh, 3, RoundingMode.CEILING));
    }

    private Optional<BigDecimal> kwhToBudget(ChargingSession session, BigDecimal energy) {
        Money reserved = funding.reserved(session.id());
        return estimator.forEnergy(session, BigDecimal.ONE)
                .map(electric -> fees.of(electric.minor()).totalMinor())
                .filter(perKwh -> perKwh > 0)
                .map(perKwh -> BigDecimal.valueOf(reserved.minor()).divide(BigDecimal.valueOf(perKwh), 3,
                        RoundingMode.FLOOR).subtract(energy).max(BigDecimal.ZERO));
    }

    private static int minutes(long seconds) {
        long rounded = (Math.max(seconds, 0) + 59) / 60;
        return (int) Math.max(1, Math.min(rounded, LONGEST_MINUTES));
    }
}
