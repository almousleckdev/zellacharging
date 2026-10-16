package com.evcharge.customer.wallet.application;

import com.evcharge.customer.wallet.application.port.LedgerRepository;
import com.evcharge.customer.wallet.application.port.TopupRepository;
import com.evcharge.customer.wallet.application.port.WalletRepository;
import com.evcharge.customer.wallet.domain.LedgerEntry;
import com.evcharge.customer.wallet.domain.LedgerType;
import com.evcharge.customer.wallet.domain.Topup;
import com.evcharge.customer.wallet.domain.TopupState;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Component
class TopupCredit {

    private final WalletSettings settings;
    private final TopupRepository topups;
    private final WalletRepository wallets;
    private final LedgerRepository ledger;
    private final Clock clock;

    TopupCredit(WalletSettings settings, TopupRepository topups, WalletRepository wallets,
                LedgerRepository ledger, Clock clock) {
        this.settings = settings;
        this.topups = topups;
        this.wallets = wallets;
        this.ledger = ledger;
        this.clock = clock;
    }

    @Transactional
    void succeed(Topup topup, String providerRef) {
        boolean moved = topups.transition(topup.id(), TopupState.PENDING, TopupState.SUCCEEDED, providerRef, null);
        if (!moved) {
            return;
        }
        wallets.ensure(topup.customerId(), settings.currency(), settings.decimals());
        wallets.lock(topup.customerId());
        wallets.apply(topup.customerId(), topup.amountMinor(), 0);
        ledger.append(new LedgerEntry(UUID.randomUUID(), topup.customerId(), LedgerType.TOPUP,
                topup.amountMinor(), null, topup.id(), "topup:" + topup.id(), clock.instant()));
    }
}
