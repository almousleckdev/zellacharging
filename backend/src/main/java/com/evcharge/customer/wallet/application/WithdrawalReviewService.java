package com.evcharge.customer.wallet.application;

import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.wallet.WithdrawalReviews;
import com.evcharge.customer.wallet.application.port.PayoutGateway;
import com.evcharge.customer.wallet.application.port.WithdrawalRepository;
import com.evcharge.customer.wallet.domain.PayoutOutcome;
import com.evcharge.customer.wallet.domain.PayoutStatus;
import com.evcharge.customer.wallet.domain.Withdrawal;
import com.evcharge.customer.wallet.domain.WithdrawalState;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.UUID;

@Service
class WithdrawalReviewService implements WithdrawalReviews {

    private final WithdrawalRepository withdrawals;
    private final WithdrawalLedger ledger;
    private final PayoutGateways gateways;
    private final PayoutDispatcher dispatcher;
    private final WithdrawalSettings settings;
    private final Clock clock;

    WithdrawalReviewService(WithdrawalRepository withdrawals, WithdrawalLedger ledger, PayoutGateways gateways,
                            PayoutDispatcher dispatcher, WithdrawalSettings settings, Clock clock) {
        this.withdrawals = withdrawals;
        this.ledger = ledger;
        this.gateways = gateways;
        this.dispatcher = dispatcher;
        this.settings = settings;
        this.clock = clock;
    }

    @Override
    public void approve(UUID adminId, UUID withdrawalId, String note) {
        Withdrawal withdrawal = awaiting(withdrawalId);
        PayoutGateway gateway = gateways.forOperator(withdrawal.operator());
        if (!ledger.approve(withdrawal, adminId, note)) {
            throw notPending();
        }
        dispatcher.send(gateway, withdrawal);
    }

    @Override
    public void reject(UUID adminId, UUID withdrawalId, String note) {
        if (!ledger.reject(awaiting(withdrawalId), adminId, note)) {
            throw notPending();
        }
    }

    @Override
    public void refund(UUID adminId, UUID withdrawalId, String note) {
        Withdrawal withdrawal = stuck(withdrawalId);
        requireNoPayoutSent(withdrawal);
        if (!ledger.refund(withdrawal, adminId, note)) {
            throw notRefundable();
        }
    }

    private Withdrawal awaiting(UUID id) {
        Withdrawal withdrawal = find(id);
        if (withdrawal.state() != WithdrawalState.PENDING_APPROVAL) {
            throw notPending();
        }
        return withdrawal;
    }

    private Withdrawal stuck(UUID id) {
        Withdrawal withdrawal = find(id);
        boolean settled = withdrawal.createdAt().plus(settings.recheckAfter()).isBefore(clock.instant());
        if (!withdrawal.state().isRefundable() || !settled) {
            throw notRefundable();
        }
        return withdrawal;
    }

    private Withdrawal find(UUID id) {
        return withdrawals.find(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                "WITHDRAWAL_NOT_FOUND", "Withdrawal not found"));
    }

    private void requireNoPayoutSent(Withdrawal withdrawal) {
        PayoutOutcome outcome = payoutStatus(withdrawal);
        boolean sent = outcome.status() == PayoutStatus.PAID
                || outcome.status() == PayoutStatus.PENDING && outcome.providerRef() != null;
        if (sent) {
            throw new ApiException(HttpStatus.CONFLICT, "WITHDRAWAL_PAYOUT_SENT",
                    "The provider has this payout, it cannot be refunded");
        }
    }

    private PayoutOutcome payoutStatus(Withdrawal withdrawal) {
        try {
            return gateways.forOperator(withdrawal.operator()).status(withdrawal.reference());
        } catch (ApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "PAYOUT_STATUS_UNAVAILABLE",
                    "The provider could not be reached to check this payout");
        }
    }

    private static ApiException notRefundable() {
        return new ApiException(HttpStatus.CONFLICT, "WITHDRAWAL_NOT_REFUNDABLE",
                "This withdrawal cannot be refunded");
    }

    private static ApiException notPending() {
        return new ApiException(HttpStatus.CONFLICT, "WITHDRAWAL_NOT_PENDING",
                "This withdrawal has already been handled");
    }
}
