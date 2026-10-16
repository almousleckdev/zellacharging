package com.evcharge.customer.charging.application.port;

import com.evcharge.customer.charging.domain.CommandOutcome;
import com.evcharge.customer.charging.domain.Metering;
import com.evcharge.customer.charging.domain.RemoteSession;

import java.util.List;
import java.util.Optional;

public interface ChargingGateway {

    boolean registerIdTag(String idTag);

    CommandOutcome start(String stationId, int connectorId, String idTag, String idempotencyKey);

    CommandOutcome stop(String stationId, int connectorId, String idempotencyKey);

    List<RemoteSession> sessionsOf(String idTag);

    Optional<Metering> metering(int transactionId);
}
