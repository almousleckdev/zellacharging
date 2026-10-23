package com.evcharge.customer.wallet.application;

import com.evcharge.customer.account.ClosureBlocker;
import com.evcharge.customer.wallet.application.port.WalletRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
class WalletBalanceBlocker implements ClosureBlocker {

    private final WalletRepository wallets;

    WalletBalanceBlocker(WalletRepository wallets) {
        this.wallets = wallets;
    }

    @Override
    public Optional<String> blockingCode(UUID customerId) {
        try {
            var wallet = wallets.read(customerId);
            return wallet.balanceMinor() > 0 || wallet.reservedMinor() > 0 ? Optional.of("WALLET_NOT_EMPTY")
                    : Optional.empty();
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
