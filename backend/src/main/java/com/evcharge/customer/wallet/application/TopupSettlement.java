package com.evcharge.customer.wallet.application;

import com.evcharge.customer.shared.TenantContext;
import com.evcharge.customer.wallet.application.port.TopupRepository;
import com.evcharge.customer.wallet.domain.PaymentOutcome;
import com.evcharge.customer.wallet.domain.Topup;
import com.evcharge.customer.wallet.domain.TopupReference;
import com.evcharge.customer.wallet.domain.TopupState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Service
public class TopupSettlement {

    private static final Logger log = LoggerFactory.getLogger(TopupSettlement.class);

    private final TopupRepository topups;
    private final PaymentGateways gateways;
    private final TopupCredit credit;
    private final PaymentSettings settings;
    private final Clock clock;

    public TopupSettlement(TopupRepository topups, PaymentGateways gateways, TopupCredit credit,
                           PaymentSettings settings, Clock clock) {
        this.topups = topups;
        this.gateways = gateways;
        this.credit = credit;
        this.settings = settings;
        this.clock = clock;
    }

    public void settleByReference(String reference) {
        Optional<String> country = TopupReference.countryOf(reference);
        if (country.isEmpty()) {
            log.warn("Payment notification with an unrecognised reference");
            return;
        }
        TenantContext.runAs(country.get(), () -> topups.findByReference(reference).ifPresentOrElse(this::settle,
                () -> log.warn("Payment notification for an unknown reference {}", reference)));
    }

    public Topup settle(Topup topup) {
        if (!topup.state().isUnsettled()) {
            return topup;
        }
        Optional<PaymentOutcome> outcome = askProvider(topup);
        outcome.ifPresent(found -> apply(topup, found));
        if (outcome.isEmpty() || outcome.get().state() == TopupState.PENDING) {
            expireIfOld(topup);
        }
        return topups.find(topup.id()).orElse(topup);
    }

    private Optional<PaymentOutcome> askProvider(Topup topup) {
        try {
            return Optional.of(gateways.forOperator(topup.operator()).status(topup.reference()));
        } catch (RuntimeException e) {
            log.warn("Could not read payment {} from the provider: {}", topup.reference(), e.getMessage());
            return Optional.empty();
        }
    }

    void apply(Topup topup, PaymentOutcome outcome) {
        switch (outcome.state()) {
            case SUCCEEDED -> credit.succeed(topup, outcome.providerRef(), outcome.confirmedMinor());
            case FAILED -> topups.transition(topup.id(), TopupState.PENDING, TopupState.FAILED, null,
                    outcome.failureCode());
            case PENDING, EXPIRED -> topups.transition(topup.id(), topup.state(), topup.state(),
                    outcome.providerRef(), null);
        }
    }

    private void expireIfOld(Topup topup) {
        Instant limit = topup.createdAt().plus(settings.expireAfter());
        if (topup.state() == TopupState.PENDING && limit.isBefore(clock.instant())) {
            topups.transition(topup.id(), TopupState.PENDING, TopupState.EXPIRED, null, "EXPIRED");
        }
    }
}
