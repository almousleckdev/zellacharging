package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.shared.Country;
import com.evcharge.customer.shared.CountryDirectory;
import com.evcharge.customer.shared.TenantContext;
import com.evcharge.customer.wallet.WalletFunding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SessionSweeper {

    private static final Logger log = LoggerFactory.getLogger(SessionSweeper.class);

    private final WalletFunding funding;
    private final SessionRepository sessions;
    private final SessionReconciler reconciler;
    private final SessionSettlement settlement;
    private final FundsGuard guard;
    private final CountryDirectory countries;

    public SessionSweeper(WalletFunding funding, SessionRepository sessions, SessionReconciler reconciler,
                          SessionSettlement settlement, FundsGuard guard,
                         CountryDirectory countries) {
        this.funding = funding;
        this.sessions = sessions;
        this.reconciler = reconciler;
        this.settlement = settlement;
        this.guard = guard;
        this.countries = countries;
    }

    @Scheduled(fixedDelayString = "${zella.charging.sweep-interval}",
            initialDelayString = "${zella.charging.sweep-interval}")
    public void sweep() {
        for (Country country : countries.active()) {
            TenantContext.runAs(country.code(), this::sweepCountry);
        }
    }

    private void sweepCountry() {
        for (UUID id : funding.openHoldSessions()) {
            try {
                sessions.findById(id).ifPresent(this::tend);
            } catch (RuntimeException e) {
                log.warn("Sweep failed for session {}", id, e);
            }
        }
    }

    private void tend(ChargingSession stored) {
        ChargingSession current = reconciler.reconcile(stored);
        if (current.state().isLive()) {
            guard.enforce(current);
        } else {
            settlement.settle(current);
        }
    }
}
