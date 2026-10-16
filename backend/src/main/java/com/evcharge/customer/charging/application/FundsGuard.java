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
import java.time.Clock;
import java.util.Optional;

@Component
public class FundsGuard {

    private static final Logger log = LoggerFactory.getLogger(FundsGuard.class);

    private final WalletFunding funding;
    private final MeteringService metering;
    private final StopCommander stopper;
    private final SessionSettings settings;
    private final Clock clock;

    public FundsGuard(WalletFunding funding, MeteringService metering, StopCommander stopper,
                      SessionSettings settings, Clock clock) {
        this.funding = funding;
        this.metering = metering;
        this.stopper = stopper;
        this.settings = settings;
        this.clock = clock;
    }

    public void enforce(ChargingSession session) {
        if (session.state() == SessionState.STOPPING) {
            retryStop(session);
        } else if (session.state() == SessionState.ACTIVE && fundsExhausted(session)) {
            log.info("Stopping session {}: reserved funds nearly consumed", session.id());
            stopper.issue(session);
        }
    }

    private void retryStop(ChargingSession session) {
        if (session.updatedAt().plus(settings.stopRetryAfter()).isBefore(clock.instant())) {
            log.warn("Session {} still stopping, re-issuing stop", session.id());
            stopper.issue(session);
        }
    }

    private boolean fundsExhausted(ChargingSession session) {
        Money cap = funding.reserved(session.id());
        return estimatedCost(session).map(cost -> cost * 100 >= cap.minor() * settings.stopThresholdPercent())
                .orElse(false);
    }

    private Optional<Long> estimatedCost(ChargingSession session) {
        if (session.costMinor() != null) {
            return Optional.of(session.costMinor());
        }
        if (settings.pricePerKwhMinor() == null) {
            return Optional.empty();
        }
        return metering.of(session).map(Metering::totalKwh)
                .map(kwh -> kwh.multiply(BigDecimal.valueOf(settings.pricePerKwhMinor()))
                        .setScale(0, java.math.RoundingMode.CEILING).longValue());
    }
}
