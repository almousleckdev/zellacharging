package com.evcharge.customer.shared;

import java.util.UUID;

public interface CustomerStanding {

    void requireActive(UUID customerId);

    void requireVerifiedContacts(UUID customerId);
}
