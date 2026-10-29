package com.evcharge.customer.account;

import java.util.UUID;

public interface CustomerModeration {

    void suspend(UUID customerId);

    void reactivate(UUID customerId);

    void delete(UUID customerId);
}
