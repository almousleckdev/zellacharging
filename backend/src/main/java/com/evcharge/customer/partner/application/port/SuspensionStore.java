package com.evcharge.customer.partner.application.port;

import com.evcharge.customer.partner.domain.ActiveSuspension;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SuspensionStore {

    List<String> knownNumbers(UUID customerId);

    List<UUID> customersWithNumber(String partner, String msisdn);

    Optional<ActiveSuspension> active(String partner, UUID customerId);

    void suspend(String partner, UUID customerId, long owedMinor, String currency, int decimals,
                 boolean blockedBefore, Collection<String> numbers, Instant at);

    void lift(String partner, UUID customerId, Instant at);
}
