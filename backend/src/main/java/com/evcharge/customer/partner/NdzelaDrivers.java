package com.evcharge.customer.partner;

import java.util.Optional;
import java.util.UUID;

public interface NdzelaDrivers {

    Optional<NdzelaDriver> find(UUID customerId);
}
