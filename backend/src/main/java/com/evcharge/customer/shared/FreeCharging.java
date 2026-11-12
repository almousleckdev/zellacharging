package com.evcharge.customer.shared;

import java.util.UUID;

public interface FreeCharging {

    boolean allowed(UUID customerId);
}
