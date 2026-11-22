package com.evcharge.customer.partner.application.port;

import java.util.UUID;

public interface NdzelaFreeCharging {

    FreeChargingEligibility check(UUID customerId);
}
