package com.evcharge.customer.charging.application.port;

import java.util.Optional;
import java.util.UUID;

public interface IdTagRepository {

    Optional<String> find(UUID customerId);

    boolean saveIfAbsent(UUID customerId, String idTag);
}
