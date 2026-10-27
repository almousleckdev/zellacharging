package com.evcharge.customer.wallet.application;

import com.evcharge.customer.shared.Country;
import com.evcharge.customer.shared.CountryDirectory;
import com.evcharge.customer.shared.TenantContext;
import com.evcharge.customer.wallet.application.port.WithdrawalRepository;
import com.evcharge.customer.wallet.domain.Withdrawal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class WithdrawalReconciliationJob {

    private static final Logger log = LoggerFactory.getLogger(WithdrawalReconciliationJob.class);

    private final WithdrawalRepository withdrawals;
    private final WithdrawalSettlement settlement;
    private final CountryDirectory countries;
    private final WithdrawalSettings settings;
    private final Clock clock;

    public WithdrawalReconciliationJob(WithdrawalRepository withdrawals, WithdrawalSettlement settlement,
                                       CountryDirectory countries, WithdrawalSettings settings, Clock clock) {
        this.withdrawals = withdrawals;
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
        for (Withdrawal withdrawal : withdrawals.findUnsettled(clock.instant().minus(settings.recheckAfter()))) {
            try {
                settlement.settle(withdrawal);
            } catch (RuntimeException e) {
                log.error("Reconciliation failed for payout {}", withdrawal.reference(), e);
            }
        }
    }
}
