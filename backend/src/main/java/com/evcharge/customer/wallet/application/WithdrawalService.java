package com.evcharge.customer.wallet.application;

import com.evcharge.customer.account.CustomerVerification;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.wallet.application.port.TopupRepository;
import com.evcharge.customer.wallet.application.port.WithdrawalRepository;
import com.evcharge.customer.wallet.domain.MobileOperator;
import com.evcharge.customer.wallet.domain.Topup;
import com.evcharge.customer.wallet.domain.Withdrawal;
import com.evcharge.customer.wallet.domain.WithdrawalState;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WithdrawalService {

    private static final Duration REFRESH_AFTER = Duration.ofSeconds(5);
    private static final int RECENT_LIMIT = 20;

    private final WithdrawalSettings settings;
    private final WithdrawalRepository withdrawals;
    private final TopupRepository topups;
    private final WithdrawalLedger ledger;
    private final WithdrawalSettlement settlement;
    private final PayoutGateways gateways;
    private final CustomerVerification verification;
    private final Clock clock;

    public WithdrawalService(WithdrawalSettings settings, WithdrawalRepository withdrawals,
                             TopupRepository topups, WithdrawalLedger ledger, WithdrawalSettlement settlement, PayoutGateways gateways,
                             CustomerVerification verification, Clock clock) {
        this.settings = settings;
        this.withdrawals = withdrawals;
        this.topups = topups;
        this.ledger = ledger;
        this.settlement = settlement;
        this.gateways = gateways;
        this.verification = verification;
        this.clock = clock;
    }

    public Withdrawal request(UUID customerId, MobileOperator operator, String requestedNumber, long amountMinor,
                              String password, String key) {
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
        gateways.forOperator(operator);
        Withdrawal pending = Withdrawal.requested(customerId, operator, payoutNumber(customerId, operator, requestedNumber),
                amountMinor, key, clock.instant());
        if (ledger.debit(pending).isEmpty()) {
            return withdrawals.findByIdempotencyKey(customerId, key).orElseThrow();
        }
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

    private String payoutNumber(UUID customerId, MobileOperator operator, String requested) {
        if (requested != null && !requested.isBlank()) {
            String number = requested.trim();
            if (!topups.hasSucceeded(customerId, operator, number)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "PAYOUT_NUMBER_NOT_ALLOWED",
                        "Money can only be sent to a number that already topped up this wallet");
            }
            return number;
        }
        return topups.lastSucceeded(customerId, operator).map(Topup::msisdn).orElseThrow(
                () -> new ApiException(HttpStatus.CONFLICT, "NO_PAYOUT_NUMBER",
                        "Top up with this operator first: money goes back to the number that paid"));
    }

    private void requireAllowedAmount(long amountMinor) {
        if (amountMinor < settings.minimumMinor() || amountMinor > settings.maximumMinor()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", "Amount is outside the allowed range");
        }
    }
}
