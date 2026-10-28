package com.evcharge.customer.wallet.application;

import com.evcharge.customer.wallet.application.port.LedgerRepository;
import com.evcharge.customer.wallet.application.port.TopupRepository;
import com.evcharge.customer.wallet.application.port.WalletRepository;
import com.evcharge.customer.wallet.domain.LedgerEntry;
import com.evcharge.customer.wallet.domain.Topup;
import com.evcharge.customer.wallet.domain.WalletBalance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WalletService {

    private static final int LEDGER_LIMIT = 50;

    private final WalletSettings settings;
    private final WalletRepository wallets;
    private final LedgerRepository ledger;
    private final TopupRepository topups;

    public WalletService(WalletSettings settings, WalletRepository wallets, LedgerRepository ledger,
                         TopupRepository topups) {
        this.settings = settings;
        this.wallets = wallets;
        this.ledger = ledger;
        this.topups = topups;
    }

    @Transactional
    public WalletBalance balance(UUID customerId) {
        wallets.ensure(customerId, settings.currency(), settings.decimals());
        return wallets.read(customerId);
    }

    public List<LedgerEntry> transactions(UUID customerId) {
        return ledger.recent(customerId, LEDGER_LIMIT);
    }

    public Optional<Topup> payoutSource(UUID customerId) {
        return topups.lastSucceeded(customerId);
    }
}
