package com.evcharge.customer.catalog.infrastructure;

import com.evcharge.customer.catalog.domain.Station;
import com.evcharge.customer.catalog.domain.StationConnector;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record OcppStationPayload(
        String id,
        String isoCode,
        boolean online,
        BigDecimal latitude,
        BigDecimal longitude,
        List<OcppConnectorPayload> connectors) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OcppConnectorPayload(int connectorId, String status, String connectorType, BigDecimal maxPowerKw) {
    }

    Station toDomain() {
        List<StationConnector> mapped = connectors == null ? List.of() : connectors.stream()
                .map(c -> new StationConnector(c.connectorId(), c.status(), c.connectorType(), c.maxPowerKw()))
                .toList();
        return new Station(id, isoCode, online, latitude, longitude, mapped);
    }
}
