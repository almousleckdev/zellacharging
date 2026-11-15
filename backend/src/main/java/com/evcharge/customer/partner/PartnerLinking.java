package com.evcharge.customer.partner;

import java.util.UUID;

public interface PartnerLinking {

    boolean linked(UUID customerId);

    boolean link(UUID customerId, UUID adminId);

    boolean unlink(UUID customerId, UUID adminId);
}
