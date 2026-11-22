package com.evcharge.customer.partner;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface PartnerLinking {

    boolean linked(UUID customerId);

    Set<UUID> linkedIds();

    Set<UUID> linkedAmong(Collection<UUID> customerIds);

    boolean link(UUID customerId, UUID adminId);

    boolean unlink(UUID customerId, UUID adminId);
}
