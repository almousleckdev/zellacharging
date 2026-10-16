package com.evcharge.customer.wallet.application;

import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.wallet.application.port.PaymentProvider;
import com.evcharge.customer.wallet.application.port.TopupRepository;
import com.evcharge.customer.wallet.domain.MobileOperator;
import com.evcharge.customer.wallet.domain.PaymentOutcome;
import com.evcharge.customer.wallet.domain.Topup;
import com.evcharge.customer.wallet.domain.TopupState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class TopupService {

    private static final Logger log = LoggerFactory.getLogger(TopupService.class);
    private static final Pattern MSISDN = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    private final WalletSettings settings;
    private final TopupRepository topups;
    private final PaymentProvider provider;
    private final TopupCredit credit;
    private final Clock clock;

    public TopupService(WalletSettings settings, TopupRepository topups, PaymentProvider provider,
                        TopupCredit credit, Clock clock) {
        this.settings = settings;
        this.topups = topups;
        this.provider = provider;
        this.credit = credit;
        this.clock = clock;
    }

    public Topup create(UUID customerId, MobileOperator operator, String msisdn, long amountMinor, String key) {
        validate(msisdn, amountMinor);
        Optional<Topup> previous = topups.findByIdempotencyKey(customerId, key);
        if (previous.isPresent()) {
            return previous.get();
        }
        Topup pending = Topup.pending(customerId, operator, msisdn, amountMinor, key, clock.instant());
        if (topups.insertIfNew(pending).isEmpty()) {
            return topups.findByIdempotencyKey(customerId, key).orElseThrow();
        }
        return apply(pending, collect(pending));
    }

    public Topup confirm(String providerRef, boolean success, String failureCode) {
        Topup topup = topups.findByProviderRef(providerRef).orElseThrow(
                () -> new ApiException(HttpStatus.NOT_FOUND, "TOPUP_NOT_FOUND", "Unknown payment reference"));
        PaymentOutcome outcome = success ? PaymentOutcome.succeeded(providerRef) : PaymentOutcome.failed(failureCode);
        return apply(topup, outcome);
    }

    public Topup get(UUID customerId, UUID id) {
        return topups.find(id).filter(topup -> topup.customerId().equals(customerId)).orElseThrow(
                () -> new ApiException(HttpStatus.NOT_FOUND, "TOPUP_NOT_FOUND", "Top-up not found"));
    }

    private PaymentOutcome collect(Topup topup) {
        try {
            return provider.collect(topup, settings.currency());
        } catch (RuntimeException e) {
            log.warn("Payment provider failed for top-up {}", topup.id(), e);
            return PaymentOutcome.failed("PROVIDER_UNAVAILABLE");
        }
    }

    private Topup apply(Topup topup, PaymentOutcome outcome) {
        switch (outcome.state()) {
            case SUCCEEDED -> credit.succeed(topup, outcome.providerRef());
            case FAILED -> topups.transition(topup.id(), TopupState.PENDING, TopupState.FAILED, null,
                    outcome.failureCode());
            case PENDING -> topups.transition(topup.id(), TopupState.PENDING, TopupState.PENDING,
                    outcome.providerRef(), null);
        }
        return topups.find(topup.id()).orElseThrow();
    }

    private void validate(String msisdn, long amountMinor) {
        if (!MSISDN.matcher(msisdn).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PHONE", "Enter a valid mobile money number");
        }
        if (amountMinor < settings.minTopupMinor() || amountMinor > settings.maxTopupMinor()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", "Amount is outside the allowed range");
        }
    }
}
