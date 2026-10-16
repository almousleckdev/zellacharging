package com.evcharge.customer.wallet.application.port;

import com.evcharge.customer.wallet.domain.WalletBalance;

import java.util.UUID;

public interface WalletRepository {

    void ensure(UUID customerId, String currency, int decimals);

    WalletBalance lock(UUID customerId);

    WalletBalance read(UUID customerId);

    void apply(UUID customerId, long balanceDelta, long reservedDelta);
}
