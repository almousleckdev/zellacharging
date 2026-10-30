package com.evcharge.customer.catalog.infrastructure;

import com.evcharge.customer.catalog.domain.Station;
import com.evcharge.customer.catalog.domain.StationConnector;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record OcppStationPayload(
        String id,
        String isoCode,
        String vendor,
        String model,
        boolean online,
        Instant lastSeenAt,
        BigDecimal latitude,
        BigDecimal longitude,
        List<OcppConnectorPayload> connectors) {

    Station toDomain() {
        List<StationConnector> mapped = connectors == null ? List.of() : connectors.stream()
                .map(c -> new StationConnector(c.connectorId(), c.status(), c.connectorType(), c.maxPowerKw()))
                .toList();
        return new Station(id, isoCode, vendor, model, online, lastSeenAt, latitude, longitude, mapped);
    }
}
