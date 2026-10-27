package com.evcharge.customer.wallet.application;

import com.evcharge.customer.shared.AuditTrail;
import com.evcharge.customer.wallet.application.port.WithdrawalRepository;
import com.evcharge.customer.wallet.domain.PayoutOutcome;
import com.evcharge.customer.wallet.domain.Withdrawal;
import com.evcharge.customer.wallet.domain.WithdrawalState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Optional;

@Service
public class WithdrawalSettlement {

    private static final Logger log = LoggerFactory.getLogger(WithdrawalSettlement.class);

    private final WithdrawalRepository withdrawals;
    private final PayoutGateways gateways;
    private final WithdrawalLedger ledger;
    private final WithdrawalSettings settings;
    private final AuditTrail audit;
    private final Clock clock;

    public WithdrawalSettlement(WithdrawalRepository withdrawals, PayoutGateways gateways, WithdrawalLedger ledger,
                                WithdrawalSettings settings, AuditTrail audit, Clock clock) {
        this.withdrawals = withdrawals;
        this.gateways = gateways;
        this.ledger = ledger;
        this.settings = settings;
        this.audit = audit;
        this.clock = clock;
    }

    public Withdrawal settle(Withdrawal withdrawal) {
        if (!withdrawal.state().isUnsettled()) {
            return withdrawal;
        }
        Optional<PayoutOutcome> outcome = askProvider(withdrawal);
        outcome.ifPresent(found -> apply(withdrawal, found));
        Withdrawal current = withdrawals.find(withdrawal.id()).orElse(withdrawal);
        if (current.state().isUnsettled()) {
            reviewIfOverdue(current);
        }
        return withdrawals.find(withdrawal.id()).orElse(withdrawal);
    }

    private Optional<PayoutOutcome> askProvider(Withdrawal withdrawal) {
        try {
            return Optional.of(gateways.forOperator(withdrawal.operator()).status(withdrawal.reference()));
        } catch (RuntimeException e) {
            log.warn("Could not read payout {} from the provider: {}", withdrawal.reference(), e.getMessage());
            return Optional.empty();
        }
    }

    private void apply(Withdrawal withdrawal, PayoutOutcome outcome) {
        switch (outcome.status()) {
            case PAID -> markPaid(withdrawal, outcome.providerRef());
            case FAILED -> ledger.reverse(withdrawal, outcome.failureCode());
            case PENDING -> {
            }
        }
    }

    private void markPaid(Withdrawal withdrawal, String providerRef) {
        boolean moved = withdrawals.transition(withdrawal.id(), WithdrawalState.PROCESSING, WithdrawalState.PAID,
                providerRef, null)
                || withdrawals.transition(withdrawal.id(), WithdrawalState.REQUESTED, WithdrawalState.PAID,
                providerRef, null);
        if (moved) {
            audit.record("WITHDRAWAL_PAID", withdrawal.customerId(), withdrawal.reference(), providerRef);
        }
    }

    private void reviewIfOverdue(Withdrawal withdrawal) {
        if (!withdrawal.createdAt().plus(settings.reviewAfter()).isBefore(clock.instant())) {
            return;
        }
        boolean moved = withdrawals.transition(withdrawal.id(), withdrawal.state(), WithdrawalState.REVIEW, null,
                "OUTCOME_UNKNOWN");
        if (moved) {
            log.error("ALERT payout {} has an unknown outcome: the money may have left, manual review needed",
                    withdrawal.reference());
            audit.record("WITHDRAWAL_REVIEW", withdrawal.customerId(), withdrawal.reference(), "OUTCOME_UNKNOWN");
        }
    }
}
