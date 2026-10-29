package com.evcharge.customer.wallet;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface WalletStatistics {

    WalletFigures figures();

    Optional<CustomerWallet> ofCustomer(UUID customerId);

    Map<UUID, CustomerWallet> ofCustomers(Collection<UUID> customerIds);
}
