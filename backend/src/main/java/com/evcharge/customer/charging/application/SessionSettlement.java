package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.wallet.WalletFunding;
import org.springframework.stereotype.Component;

@Component
public class SessionSettlement {

    private final WalletFunding funding;

    public SessionSettlement(WalletFunding funding) {
        this.funding = funding;
    }

    public void settle(ChargingSession session) {
        switch (session.state()) {
            case COMPLETED, ABANDONED -> funding.settle(session.id(), session.costMinor(), session.currency());
            case FAILED -> funding.release(session.id());
            default -> {
            }
        }
    }
}
