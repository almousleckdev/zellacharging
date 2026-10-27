package com.evcharge.customer.wallet.application;

import com.evcharge.customer.shared.Country;
import com.evcharge.customer.shared.CountryDirectory;
import com.evcharge.customer.shared.TenantContext;
import com.evcharge.customer.wallet.application.port.LedgerRepository;
import com.evcharge.customer.wallet.domain.LedgerDiscrepancy;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LedgerReconciliationJob {

    private static final Logger log = LoggerFactory.getLogger(LedgerReconciliationJob.class);

    private final LedgerRepository ledger;
    private final CountryDirectory countries;
    private final Counter discrepancies;

    public LedgerReconciliationJob(LedgerRepository ledger, CountryDirectory countries, MeterRegistry registry) {
        this.ledger = ledger;
        this.countries = countries;
        this.discrepancies = registry.counter("zella.wallet.ledger.discrepancies");
    }

    @Scheduled(fixedDelayString = "${zella.wallet.ledger-check-interval}",
            initialDelayString = "${zella.wallet.ledger-check-interval}")
    public void run() {
        for (Country country : countries.active()) {
            TenantContext.runAs(country.code(), () -> check(country.code()));
        }
    }

    public List<LedgerDiscrepancy> check(String country) {
        List<LedgerDiscrepancy> found = ledger.discrepancies();
        found.forEach(item -> {
            discrepancies.increment();
            log.error("ALERT wallet {} ({}) does not match its ledger: balance {} expected {}, reserved {} expected {}",
                    item.customerId(), country, item.balanceMinor(), item.expectedBalanceMinor(),
                    item.reservedMinor(), item.expectedReservedMinor());
        });
        return found;
    }
}
