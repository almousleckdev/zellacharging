package com.evcharge.customer.partner.application.port;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LinkStore {

    boolean link(String partner, UUID customerId, Instant at);

    boolean unlink(String partner, UUID customerId);

    List<UUID> linkedCustomerIds(String partner);
}
