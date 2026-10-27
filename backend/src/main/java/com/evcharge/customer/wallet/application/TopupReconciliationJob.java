package com.evcharge.customer.wallet.application;

import com.evcharge.customer.shared.Country;
import com.evcharge.customer.shared.CountryDirectory;
import com.evcharge.customer.shared.TenantContext;
import com.evcharge.customer.wallet.application.port.TopupRepository;
import com.evcharge.customer.wallet.domain.Topup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
public class TopupReconciliationJob {

    private static final Logger log = LoggerFactory.getLogger(TopupReconciliationJob.class);

    private final TopupRepository topups;
    private final TopupSettlement settlement;
    private final CountryDirectory countries;
    private final PaymentSettings settings;
    private final Clock clock;

    public TopupReconciliationJob(TopupRepository topups, TopupSettlement settlement, CountryDirectory countries,
                                  PaymentSettings settings, Clock clock) {
        this.topups = topups;
        this.settlement = settlement;
        this.countries = countries;
        this.settings = settings;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${zella.payment.reconcile-interval}",
            initialDelayString = "${zella.payment.reconcile-interval}")
    public void run() {
        for (Country country : countries.active()) {
            TenantContext.runAs(country.code(), this::recheckUnsettled);
        }
    }

    private void recheckUnsettled() {
        Instant now = clock.instant();
        for (Topup topup : topups.findUnsettled(now.minus(settings.lateWindow()), now.minus(settings.recheckAfter()))) {
            try {
                settlement.settle(topup);
            } catch (RuntimeException e) {
                log.error("Reconciliation failed for payment {}", topup.reference(), e);
            }
        }
    }
}
