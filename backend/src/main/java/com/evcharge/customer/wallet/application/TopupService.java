package com.evcharge.customer.wallet.application;

import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.CustomerStanding;
import com.evcharge.customer.shared.NotificationType;
import com.evcharge.customer.shared.SuspendedNumbers;
import com.evcharge.customer.shared.Notifier;
import com.evcharge.customer.wallet.application.port.TopupRepository;
import com.evcharge.customer.wallet.domain.CollectionRequest;
import com.evcharge.customer.wallet.domain.MobileOperator;
import com.evcharge.customer.wallet.domain.PaymentOutcome;
import com.evcharge.customer.wallet.domain.Topup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class TopupService {

    private static final Logger log = LoggerFactory.getLogger(TopupService.class);
    private static final Pattern MSISDN = Pattern.compile("^\\+[1-9]\\d{7,14}$");
    private static final Duration REFRESH_AFTER = Duration.ofSeconds(5);

    private final WalletSettings settings;
    private final TopupRepository topups;
    private final PaymentGateways gateways;
    private final TopupSettlement settlement;
    private final PaymentSettings payments;
    private final Clock clock;
    private final Notifier notifier;
    private final CustomerStanding standing;
    private final SuspendedNumbers suspendedNumbers;

    public TopupService(WalletSettings settings, TopupRepository topups, PaymentGateways gateways,
                        TopupSettlement settlement, PaymentSettings payments,
                        Clock clock, Notifier notifier, CustomerStanding standing,
                        SuspendedNumbers suspendedNumbers) {
        this.settings = settings;
        this.topups = topups;
        this.gateways = gateways;
        this.settlement = settlement;
        this.payments = payments;
        this.clock = clock;
        this.notifier = notifier;
        this.standing = standing;
        this.suspendedNumbers = suspendedNumbers;
    }

    public Topup create(UUID customerId, MobileOperator operator, String msisdn, long amountMinor, String key) {
        standing.requireActive(customerId);
        validate(msisdn, amountMinor);
        if (suspendedNumbers.refuse(customerId, msisdn, amountMinor, settings.currency(), key)) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "TOPUP_NUMBER_SUSPENDED",
                    "This number cannot be used to top up");
        }
        Optional<Topup> previous = topups.findByIdempotencyKey(customerId, key);
        if (previous.isPresent()) {
            return previous.get();
        }
        gateways.forOperator(operator);
        if (topups.hasPending(customerId, clock.instant().minus(payments.expireAfter()))) {
            throw new ApiException(HttpStatus.CONFLICT, "TOPUP_PENDING",
                    "A payment is still being verified");
        }
        Topup pending = Topup.pending(customerId, operator, msisdn, amountMinor, key, clock.instant());
        if (topups.insertIfNew(pending).isEmpty()) {
            return topups.findByIdempotencyKey(customerId, key).orElseThrow();
        }
        notifier.notify(customerId, NotificationType.TOPUP_STARTED, "topup:" + pending.id() + ":start",
                Map.of("screen", "wallet"));
        collect(pending).ifPresent(outcome -> settlement.apply(pending, outcome));
        return topups.find(pending.id()).orElseThrow();
    }

    public Topup get(UUID customerId, UUID id) {
        Topup topup = topups.find(id).filter(found -> found.customerId().equals(customerId)).orElseThrow(
                () -> new ApiException(HttpStatus.NOT_FOUND, "TOPUP_NOT_FOUND", "Top-up not found"));
        boolean stale = topup.updatedAt().plus(REFRESH_AFTER).isBefore(clock.instant());
        return topup.state().isUnsettled() && stale ? settlement.settle(topup) : topup;
    }

    private Optional<PaymentOutcome> collect(Topup topup) {
        CollectionRequest request = new CollectionRequest(topup.reference(), topup.amountMinor(),
                settings.currency(), topup.operator(), topup.msisdn());
        try {
            return Optional.of(gateways.forOperator(topup.operator()).collect(request));
        } catch (RuntimeException e) {
            log.warn("Payment {} was sent but its outcome is unknown: {}", topup.reference(), e.getMessage());
            return Optional.empty();
        }
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
