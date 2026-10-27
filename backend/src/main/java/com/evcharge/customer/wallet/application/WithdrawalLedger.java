package com.evcharge.customer.wallet.application;

import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.AuditTrail;
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
import java.util.Optional;
import java.util.UUID;

@Component
class WithdrawalLedger {

    private final WithdrawalRepository withdrawals;
    private final WalletRepository wallets;
    private final LedgerRepository ledger;
    private final AuditTrail audit;
    private final WalletSettings settings;
    private final Clock clock;

    WithdrawalLedger(WithdrawalRepository withdrawals, WalletRepository wallets, LedgerRepository ledger,
                     AuditTrail audit, WalletSettings settings, Clock clock) {
        this.withdrawals = withdrawals;
        this.wallets = wallets;
        this.ledger = ledger;
        this.audit = audit;
        this.settings = settings;
        this.clock = clock;
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
            wallets.apply(withdrawal.customerId(), -withdrawal.amountMinor(), 0);
            ledger.append(entry(withdrawal, LedgerType.WITHDRAWAL, "withdrawal:" + withdrawal.id()));
            audit.record("WITHDRAWAL_REQUESTED", withdrawal.customerId(), withdrawal.reference(),
                    Long.toString(withdrawal.amountMinor()));
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
            wallets.lock(withdrawal.customerId());
            wallets.apply(withdrawal.customerId(), withdrawal.amountMinor(), 0);
            ledger.append(entry(withdrawal, LedgerType.WITHDRAWAL_REVERSAL, "withdrawal-reversal:" + withdrawal.id()));
            audit.record("WITHDRAWAL_REVERSED", withdrawal.customerId(), withdrawal.reference(), failureCode);
        }
        return moved;
    }

    private LedgerEntry entry(Withdrawal withdrawal, LedgerType type, String key) {
        return new LedgerEntry(UUID.randomUUID(), withdrawal.customerId(), type, withdrawal.amountMinor(), null, null,
                key, clock.instant());
    }
}
