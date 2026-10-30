package com.evcharge.customer.wallet.application;

import com.evcharge.customer.wallet.application.port.PayoutGateway;
import com.evcharge.customer.wallet.application.port.WithdrawalRepository;
import com.evcharge.customer.wallet.domain.PayoutOutcome;
import com.evcharge.customer.wallet.domain.PayoutRequest;
import com.evcharge.customer.wallet.domain.Withdrawal;
import com.evcharge.customer.wallet.domain.WithdrawalState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class PayoutDispatcher {

    private static final Logger log = LoggerFactory.getLogger(PayoutDispatcher.class);

    private final WithdrawalRepository withdrawals;
    private final WithdrawalLedger ledger;
    private final WalletSettings wallet;

    PayoutDispatcher(WithdrawalRepository withdrawals, WithdrawalLedger ledger, WalletSettings wallet) {
        this.withdrawals = withdrawals;
        this.ledger = ledger;
        this.wallet = wallet;
    }

    void send(PayoutGateway gateway, Withdrawal withdrawal) {
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
}
