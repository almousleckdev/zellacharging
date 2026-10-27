package com.evcharge.customer.account;

import java.util.Optional;
import java.util.UUID;

public interface CustomerDirectory {

    CustomerPage search(CustomerQuery query);

    Optional<CustomerRecord> find(UUID id);
}
