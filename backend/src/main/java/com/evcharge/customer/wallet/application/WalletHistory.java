package com.evcharge.customer.wallet.application;

import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.wallet.application.port.LedgerRepository;
import com.evcharge.customer.wallet.application.port.TopupRepository;
import com.evcharge.customer.wallet.application.port.WithdrawalRepository;
import com.evcharge.customer.wallet.domain.LedgerDetail;
import com.evcharge.customer.wallet.domain.LedgerEntry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

@Service
public class WalletHistory {

    private final LedgerRepository ledger;
    private final TopupRepository topups;
    private final WithdrawalRepository withdrawals;
    private final Clock clock;

    public WalletHistory(LedgerRepository ledger, TopupRepository topups, WithdrawalRepository withdrawals,
                         Clock clock) {
        this.ledger = ledger;
        this.topups = topups;
        this.withdrawals = withdrawals;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public LedgerDetail detail(UUID customerId, UUID id) {
        LedgerEntry entry = ledger.find(customerId, id).orElseThrow(WalletHistory::notFound);
        return new LedgerDetail(entry, ledger.balanceAfter(customerId, entry.createdAt()),
                Optional.ofNullable(entry.topupId()).flatMap(topups::find),
                entry.withdrawalId().flatMap(withdrawals::find));
    }

    @Transactional
    public void hide(UUID customerId, UUID id) {
        ledger.hide(customerId, id, clock.instant());
    }

    private static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "TRANSACTION_NOT_FOUND", "Transaction not found");
    }
}
