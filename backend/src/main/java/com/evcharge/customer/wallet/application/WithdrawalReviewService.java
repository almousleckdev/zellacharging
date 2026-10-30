package com.evcharge.customer.wallet.application;

import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.wallet.WithdrawalReviews;
import com.evcharge.customer.wallet.application.port.PayoutGateway;
import com.evcharge.customer.wallet.application.port.WithdrawalRepository;
import com.evcharge.customer.wallet.domain.Withdrawal;
import com.evcharge.customer.wallet.domain.WithdrawalState;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
class WithdrawalReviewService implements WithdrawalReviews {

    private final WithdrawalRepository withdrawals;
    private final WithdrawalLedger ledger;
    private final PayoutGateways gateways;
    private final PayoutDispatcher dispatcher;

    WithdrawalReviewService(WithdrawalRepository withdrawals, WithdrawalLedger ledger, PayoutGateways gateways,
                            PayoutDispatcher dispatcher) {
        this.withdrawals = withdrawals;
        this.ledger = ledger;
        this.gateways = gateways;
        this.dispatcher = dispatcher;
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

    private Withdrawal awaiting(UUID id) {
        Withdrawal withdrawal = withdrawals.find(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                "WITHDRAWAL_NOT_FOUND", "Withdrawal not found"));
        if (withdrawal.state() != WithdrawalState.PENDING_APPROVAL) {
            throw notPending();
        }
        return withdrawal;
    }

    private static ApiException notPending() {
        return new ApiException(HttpStatus.CONFLICT, "WITHDRAWAL_NOT_PENDING",
                "This withdrawal has already been handled");
    }
}
