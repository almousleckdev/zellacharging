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

    public SessionSettlement(WalletFunding funding) {
        this.funding = funding;
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
            log.warn("Session {} finished without a cost: the hold is released without a charge", session.id());
        }
        funding.settle(session.id(), session.costMinor(), session.currency());
    }
}
