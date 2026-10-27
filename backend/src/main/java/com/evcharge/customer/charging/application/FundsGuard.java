package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.Metering;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.Money;
import com.evcharge.customer.wallet.WalletFunding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.util.Optional;

@Component
public class FundsGuard {

    private static final Logger log = LoggerFactory.getLogger(FundsGuard.class);
    private static final BigDecimal SECONDS_PER_HOUR = BigDecimal.valueOf(3600);

    private final WalletFunding funding;
    private final MeteringService metering;
    private final StopCommander stopper;
    private final SessionSettings settings;
    private final SessionTariff tariff;
    private final FeeCalculator fees;
    private final ChargingMetrics metrics;
    private final Clock clock;

    public FundsGuard(WalletFunding funding, MeteringService metering, StopCommander stopper,
                      SessionSettings settings, SessionTariff tariff, FeeCalculator fees, ChargingMetrics metrics,
                      Clock clock) {
        this.funding = funding;
        this.metering = metering;
        this.stopper = stopper;
        this.settings = settings;
        this.tariff = tariff;
        this.fees = fees;
        this.metrics = metrics;
        this.clock = clock;
    }

    public void enforce(ChargingSession session) {
        if (session.state() == SessionState.STOPPING) {
            retryStop(session);
        } else if (session.state() == SessionState.ACTIVE) {
            metering.of(session).ifPresent(reading -> enforce(session, reading));
        }
    }

    private void enforce(ChargingSession session, Metering reading) {
        Optional<Long> price = tariff.pricePerKwhMinor(session.id());
        if (price.isEmpty()) {
            log.error("ALERT session {} is charging with no known tariff: the budget cannot be enforced, stopping",
                    session.id());
            metrics.stopWithoutTariff();
            stopper.issue(session);
        } else if (budgetReached(session, reading, price.get())) {
            log.info("Stopping session {}: the authorized budget is about to be consumed", session.id());
            stopper.issue(session);
        }
    }

    private void retryStop(ChargingSession session) {
        if (session.updatedAt().plus(settings.stopRetryAfter()).isBefore(clock.instant())) {
            log.warn("Session {} still stopping, re-issuing stop", session.id());
            stopper.issue(session);
        }
    }

    private boolean budgetReached(ChargingSession session, Metering reading, long price) {
        Money cap = funding.reserved(session.id());
        long expected = costNow(session, reading, price) + costAhead(reading, price);
        return expected * 100 >= cap.minor() * settings.stopThresholdPercent();
    }

    private long costNow(ChargingSession session, Metering reading, long price) {
        if (session.costMinor() != null) {
            return session.costMinor();
        }
        return totalFor(reading.totalKwh(), price);
    }

    private long costAhead(Metering reading, long price) {
        if (reading.powerKw() == null || reading.powerKw().signum() <= 0) {
            return 0;
        }
        BigDecimal hours = BigDecimal.valueOf(settings.stopLeadTime().toSeconds())
                .divide(SECONDS_PER_HOUR, 6, RoundingMode.CEILING);
        return totalFor(reading.powerKw().multiply(hours), price);
    }

    private long totalFor(BigDecimal kwh, long price) {
        long electric = kwh.multiply(BigDecimal.valueOf(price)).setScale(0, RoundingMode.CEILING).longValue();
        return fees.of(electric).totalMinor();
    }
}
