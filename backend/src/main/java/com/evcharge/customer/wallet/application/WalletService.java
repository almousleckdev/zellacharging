package com.evcharge.customer.wallet.application;

import com.evcharge.customer.wallet.application.port.LedgerRepository;
import com.evcharge.customer.wallet.application.port.WalletRepository;
import com.evcharge.customer.wallet.domain.LedgerEntry;
import com.evcharge.customer.wallet.domain.WalletBalance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class WalletService {

    private static final int LEDGER_LIMIT = 50;

    private final WalletSettings settings;
    private final WalletRepository wallets;
    private final LedgerRepository ledger;

    public WalletService(WalletSettings settings, WalletRepository wallets, LedgerRepository ledger) {
        this.settings = settings;
        this.wallets = wallets;
        this.ledger = ledger;
    }

    @Transactional
    public WalletBalance balance(UUID customerId) {
        wallets.ensure(customerId, settings.currency(), settings.decimals());
        return wallets.read(customerId);
    }

    public List<LedgerEntry> transactions(UUID customerId) {
        return ledger.recent(customerId, LEDGER_LIMIT);
    }
}
