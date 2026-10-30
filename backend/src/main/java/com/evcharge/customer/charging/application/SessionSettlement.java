package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.NotificationType;
import com.evcharge.customer.shared.Notifier;
import com.evcharge.customer.wallet.WalletFunding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
public class SessionSettlement {

    private static final Logger log = LoggerFactory.getLogger(SessionSettlement.class);

    private final WalletFunding funding;
    private final FeeCalculator fees;
    private final ChargingMetrics metrics;
    private final Notifier notifier;

    public SessionSettlement(WalletFunding funding, FeeCalculator fees, ChargingMetrics metrics,
                             Notifier notifier) {
        this.funding = funding;
        this.fees = fees;
        this.metrics = metrics;
        this.notifier = notifier;
    }

    @Transactional
    public void settle(ChargingSession session) {
        switch (session.state()) {
            case COMPLETED -> {
                settleFinished(session);
                announce(session, NotificationType.CHARGE_COMPLETED);
            }
            case ABANDONED -> {
                settleFinished(session);
                announce(session, NotificationType.CHARGE_INTERRUPTED);
            }
            case FAILED -> {
                funding.release(session.id());
                announce(session, NotificationType.CHARGE_FAILED);
            }
            default -> {
            }
        }
    }

    @Transactional
    public void onChange(ChargingSession before, ChargingSession after) {
        settle(after);
        if (before.state() != SessionState.ACTIVE && after.state() == SessionState.ACTIVE) {
            announce(after, NotificationType.CHARGE_STARTED);
        }
    }

    private void announce(ChargingSession session, NotificationType type) {
        notifier.notify(session.customerId(), type, "session:" + session.id() + ":" + type,
                Map.of("screen", "charging", "id", session.id().toString()));
    }

    private void settleFinished(ChargingSession session) {
        if (session.costMinor() == null) {
            metrics.settlementReview();
            log.error("ALERT session {} finished without a cost: nothing is charged, flagged for review",
                    session.id());
            funding.settle(session.id(), null, session.currency());
            return;
        }
        long total = fees.of(session.costMinor()).totalMinor();
        long authorized = funding.reserved(session.id()).minor();
        if (total > authorized) {
            metrics.overshoot(total - authorized);
            log.warn("Session {} cost {} exceeds the authorized {}: charging the authorized amount only",
                    session.id(), total, authorized);
        }
        funding.settle(session.id(), total, session.currency());
    }
}
