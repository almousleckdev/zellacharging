package com.evcharge.customer.charging.application.port;

import com.evcharge.customer.charging.domain.ChargingSession;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SessionRepository {

    default void insert(ChargingSession session) {
        insert(session, null);
    }

    void insert(ChargingSession session, String idempotencyKey);

    Optional<ChargingSession> findByIdempotencyKey(UUID customerId, String idempotencyKey);

    void update(ChargingSession session);

    Optional<ChargingSession> findOwned(UUID id, UUID customerId);

    Optional<ChargingSession> findById(UUID id);

    Optional<ChargingSession> findLive(UUID customerId);

    Optional<ChargingSession> findLiveAt(String stationId, int connectorId, String idTag);

    boolean hide(UUID id, UUID customerId, Instant at);

    Map<String, Set<Integer>> liveConnectors();

    List<ChargingSession> history(UUID customerId, int limit);
}
