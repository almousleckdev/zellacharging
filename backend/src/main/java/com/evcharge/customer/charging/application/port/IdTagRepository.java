package com.evcharge.customer.charging.application.port;

import java.util.Optional;
import java.util.UUID;

public interface IdTagRepository {

    Optional<String> find(UUID customerId);

    Optional<UUID> customerOf(String idTag);

    boolean saveIfAbsent(UUID customerId, String idTag);

    void replace(UUID customerId, String idTag);
}
