package com.evcharge.customer.wallet;

import java.util.Optional;
import java.util.UUID;

public interface WalletStatistics {

    WalletFigures figures();

    Optional<CustomerWallet> ofCustomer(UUID customerId);
}
