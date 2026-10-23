package com.evcharge.customer.charging.application.port;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.LastReading;
import com.evcharge.customer.charging.domain.Metering;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SessionRepository {

    void insert(ChargingSession session);

    void update(ChargingSession session);

    Optional<ChargingSession> findOwned(UUID id, UUID customerId);

    Optional<ChargingSession> findById(UUID id);

    Optional<ChargingSession> findLive(UUID customerId);

    void recordReading(UUID id, Metering reading);

    LastReading lastReadingOf(UUID id);

    boolean hide(UUID id, UUID customerId, Instant at);

    void recordPrice(UUID id, long pricePerKwhMinor);

    Optional<Long> priceOf(UUID id);

    Map<String, Set<Integer>> liveConnectors();

    List<ChargingSession> history(UUID customerId, int limit);
}
