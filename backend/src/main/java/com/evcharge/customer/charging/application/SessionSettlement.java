package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.wallet.WalletFunding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SessionSettlement {

    private static final Logger log = LoggerFactory.getLogger(SessionSettlement.class);

    private final WalletFunding funding;
    private final FeeCalculator fees;
    private final ChargingMetrics metrics;

    public SessionSettlement(WalletFunding funding, FeeCalculator fees, ChargingMetrics metrics) {
        this.funding = funding;
        this.fees = fees;
        this.metrics = metrics;
    }

    public void settle(ChargingSession session) {
        switch (session.state()) {
            case COMPLETED, ABANDONED -> settleFinished(session);
            case FAILED -> funding.release(session.id());
            default -> {
            }
        }
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
