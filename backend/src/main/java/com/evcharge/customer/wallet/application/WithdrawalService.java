package com.evcharge.customer.wallet.application;

import com.evcharge.customer.account.CustomerVerification;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.wallet.application.port.PayoutGateway;
import com.evcharge.customer.wallet.application.port.WithdrawalRepository;
import com.evcharge.customer.wallet.domain.MobileOperator;
import com.evcharge.customer.wallet.domain.PayoutOutcome;
import com.evcharge.customer.wallet.domain.PayoutRequest;
import com.evcharge.customer.wallet.domain.Withdrawal;
import com.evcharge.customer.wallet.domain.WithdrawalState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WithdrawalService {

    private static final Logger log = LoggerFactory.getLogger(WithdrawalService.class);
    private static final Duration REFRESH_AFTER = Duration.ofSeconds(5);
    private static final int RECENT_LIMIT = 20;

    private final WithdrawalSettings settings;
    private final WalletSettings wallet;
    private final WithdrawalRepository withdrawals;
    private final WithdrawalLedger ledger;
    private final WithdrawalSettlement settlement;
    private final PayoutGateways gateways;
    private final CustomerVerification verification;
    private final Clock clock;

    public WithdrawalService(WithdrawalSettings settings, WalletSettings wallet, WithdrawalRepository withdrawals,
                             WithdrawalLedger ledger, WithdrawalSettlement settlement, PayoutGateways gateways,
                             CustomerVerification verification, Clock clock) {
        this.settings = settings;
        this.wallet = wallet;
        this.withdrawals = withdrawals;
        this.ledger = ledger;
        this.settlement = settlement;
        this.gateways = gateways;
        this.verification = verification;
        this.clock = clock;
    }

    public Withdrawal request(UUID customerId, MobileOperator operator, long amountMinor, String password,
                              String key) {
        if (!settings.enabled()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "WITHDRAWALS_UNAVAILABLE",
                    "Withdrawals are not available yet");
        }
        Optional<Withdrawal> earlier = withdrawals.findByIdempotencyKey(customerId, key);
        if (earlier.isPresent()) {
            return earlier.get();
        }
        verification.confirmPassword(customerId, password);
        requireAllowedAmount(amountMinor);
        PayoutGateway gateway = gateways.forOperator(operator);
        Withdrawal pending = Withdrawal.requested(customerId, operator, verification.verifiedPhone(customerId),
                amountMinor, key, clock.instant());
        if (ledger.debit(pending).isEmpty()) {
            return withdrawals.findByIdempotencyKey(customerId, key).orElseThrow();
        }
        dispatch(gateway, pending);
        return withdrawals.find(pending.id()).orElseThrow();
    }

    public Withdrawal get(UUID customerId, UUID id) {
        Withdrawal withdrawal = withdrawals.find(id).filter(found -> found.customerId().equals(customerId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "WITHDRAWAL_NOT_FOUND",
                        "Withdrawal not found"));
        boolean stale = withdrawal.updatedAt().plus(REFRESH_AFTER).isBefore(clock.instant());
        return withdrawal.state().isUnsettled() && stale ? settlement.settle(withdrawal) : withdrawal;
    }

    public List<Withdrawal> recent(UUID customerId) {
        return withdrawals.recent(customerId, RECENT_LIMIT);
    }

    private void dispatch(PayoutGateway gateway, Withdrawal withdrawal) {
        PayoutRequest request = new PayoutRequest(withdrawal.reference(), withdrawal.amountMinor(),
                wallet.currency(), withdrawal.operator(), withdrawal.msisdn());
        try {
            PayoutOutcome outcome = gateway.payout(request);
            switch (outcome.status()) {
                case PENDING, PAID -> withdrawals.transition(withdrawal.id(), WithdrawalState.REQUESTED,
                        WithdrawalState.PROCESSING, outcome.providerRef(), null);
                case FAILED -> ledger.reverse(withdrawal, outcome.failureCode());
            }
        } catch (RuntimeException e) {
            log.warn("Payout {} was sent but its outcome is unknown: {}", withdrawal.reference(), e.getMessage());
        }
    }

    private void requireAllowedAmount(long amountMinor) {
        if (amountMinor < settings.minimumMinor() || amountMinor > settings.maximumMinor()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", "Amount is outside the allowed range");
        }
    }
}
