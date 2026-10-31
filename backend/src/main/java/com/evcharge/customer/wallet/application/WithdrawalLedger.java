package com.evcharge.customer.wallet.application;

import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.AuditTrail;
import com.evcharge.customer.shared.NotificationType;
import com.evcharge.customer.shared.Notifier;
import com.evcharge.customer.wallet.application.port.LedgerRepository;
import com.evcharge.customer.wallet.application.port.WalletRepository;
import com.evcharge.customer.wallet.application.port.WithdrawalRepository;
import com.evcharge.customer.wallet.domain.LedgerEntry;
import com.evcharge.customer.wallet.domain.LedgerType;
import com.evcharge.customer.wallet.domain.WalletBalance;
import com.evcharge.customer.wallet.domain.Withdrawal;
import com.evcharge.customer.wallet.domain.WithdrawalState;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
class WithdrawalLedger {

    private final WithdrawalRepository withdrawals;
    private final WalletRepository wallets;
    private final LedgerRepository ledger;
    private final AuditTrail audit;
    private final WalletSettings settings;
    private final DailyWithdrawalLimit dailyLimit;
    private final Clock clock;
    private final Notifier notifier;

    WithdrawalLedger(WithdrawalRepository withdrawals, WalletRepository wallets, LedgerRepository ledger,
                     AuditTrail audit, WalletSettings settings, DailyWithdrawalLimit dailyLimit, Clock clock,
                     Notifier notifier) {
        this.withdrawals = withdrawals;
        this.wallets = wallets;
        this.ledger = ledger;
        this.audit = audit;
        this.settings = settings;
        this.dailyLimit = dailyLimit;
        this.clock = clock;
        this.notifier = notifier;
    }

    @Transactional
    Optional<Withdrawal> debit(Withdrawal withdrawal) {
        wallets.ensure(withdrawal.customerId(), settings.currency(), settings.decimals());
        WalletBalance wallet = wallets.lock(withdrawal.customerId());
        if (wallet.availableMinor() < withdrawal.amountMinor()) {
            throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "INSUFFICIENT_FUNDS",
                    "The wallet balance is too low");
        }
        Optional<Withdrawal> inserted = withdrawals.insertIfNew(withdrawal);
        if (inserted.isPresent()) {
            dailyLimit.requireNotExceeded(withdrawal.customerId());
            wallets.apply(withdrawal.customerId(), -withdrawal.amountMinor(), 0);
            ledger.append(entry(withdrawal, LedgerType.WITHDRAWAL, "withdrawal:" + withdrawal.id()));
            audit.record("WITHDRAWAL_REQUESTED", withdrawal.customerId(), withdrawal.reference(),
                    Long.toString(withdrawal.amountMinor()));
            announce(withdrawal, NotificationType.WITHDRAWAL_STARTED);
        }
        return inserted;
    }

    @Transactional
    boolean reverse(Withdrawal withdrawal, String failureCode) {
        boolean moved = withdrawals.transition(withdrawal.id(), WithdrawalState.REQUESTED, WithdrawalState.FAILED,
                null, failureCode)
                || withdrawals.transition(withdrawal.id(), WithdrawalState.PROCESSING, WithdrawalState.FAILED,
                null, failureCode);
        if (moved) {
            restore(withdrawal);
            audit.record("WITHDRAWAL_REVERSED", withdrawal.customerId(), withdrawal.reference(), failureCode);
            announce(withdrawal, NotificationType.WITHDRAWAL_FAILED);
        }
        return moved;
    }

    @Transactional
    boolean reject(Withdrawal withdrawal, UUID adminId, String note) {
        boolean moved = withdrawals.review(withdrawal.id(), WithdrawalState.PENDING_APPROVAL, WithdrawalState.FAILED,
                adminId, note, "REJECTED", clock.instant());
        if (moved) {
            restore(withdrawal);
            audit.record("WITHDRAWAL_REJECTED", withdrawal.customerId(), withdrawal.reference(), note);
            announce(withdrawal, NotificationType.WITHDRAWAL_REJECTED);
        }
        return moved;
    }

    @Transactional
    boolean refund(Withdrawal withdrawal, UUID adminId, String note) {
        boolean moved = Arrays.stream(WithdrawalState.values()).filter(WithdrawalState::isRefundable)
                .anyMatch(from -> withdrawals.review(withdrawal.id(), from, WithdrawalState.FAILED, adminId, note,
                        "REFUNDED", clock.instant()));
        if (moved) {
            restore(withdrawal);
            audit.record("WITHDRAWAL_REFUNDED", withdrawal.customerId(), withdrawal.reference(), note);
            announce(withdrawal, NotificationType.WITHDRAWAL_FAILED);
        }
        return moved;
    }

    @Transactional
    boolean approve(Withdrawal withdrawal, UUID adminId, String note) {
        boolean moved = withdrawals.review(withdrawal.id(), WithdrawalState.PENDING_APPROVAL,
                WithdrawalState.REQUESTED, adminId, note, null, clock.instant());
        if (moved) {
            audit.record("WITHDRAWAL_APPROVED", withdrawal.customerId(), withdrawal.reference(), note);
            announce(withdrawal, NotificationType.WITHDRAWAL_APPROVED);
        }
        return moved;
    }

    @Transactional
    boolean markPaid(Withdrawal withdrawal, String providerRef) {
        boolean moved = withdrawals.transition(withdrawal.id(), WithdrawalState.PROCESSING, WithdrawalState.PAID,
                providerRef, null)
                || withdrawals.transition(withdrawal.id(), WithdrawalState.REQUESTED, WithdrawalState.PAID,
                providerRef, null);
        if (moved) {
            audit.record("WITHDRAWAL_PAID", withdrawal.customerId(), withdrawal.reference(), providerRef);
            announce(withdrawal, NotificationType.WITHDRAWAL_SUCCEEDED);
        }
        return moved;
    }

    private void restore(Withdrawal withdrawal) {
        wallets.lock(withdrawal.customerId());
        wallets.apply(withdrawal.customerId(), withdrawal.amountMinor(), 0);
        ledger.append(entry(withdrawal, LedgerType.WITHDRAWAL_REVERSAL, "withdrawal-reversal:" + withdrawal.id()));
    }

    private void announce(Withdrawal withdrawal, NotificationType type) {
        notifier.notify(withdrawal.customerId(), type, "withdrawal:" + withdrawal.id() + ":" + type,
                Map.of("screen", "wallet"));
    }

    private LedgerEntry entry(Withdrawal withdrawal, LedgerType type, String key) {
        return new LedgerEntry(UUID.randomUUID(), withdrawal.customerId(), type, withdrawal.amountMinor(), null, null,
                key, clock.instant());
    }
}
