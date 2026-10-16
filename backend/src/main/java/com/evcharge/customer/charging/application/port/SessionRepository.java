package com.evcharge.customer.charging.application.port;

import com.evcharge.customer.charging.domain.ChargingSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository {

    void insert(ChargingSession session);

    void update(ChargingSession session);

    Optional<ChargingSession> findOwned(UUID id, UUID customerId);

    Optional<ChargingSession> findById(UUID id);

    Optional<ChargingSession> findLive(UUID customerId);

    List<ChargingSession> history(UUID customerId, int limit);
}
