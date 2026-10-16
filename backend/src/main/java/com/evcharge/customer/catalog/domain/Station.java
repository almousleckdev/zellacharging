package com.evcharge.customer.catalog.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public record Station(
        String id,
        String isoCode,
        boolean online,
        BigDecimal latitude,
        BigDecimal longitude,
        List<StationConnector> connectors) {

    public Optional<StationConnector> connector(int connectorId) {
        return connectors.stream().filter(c -> c.connectorId() == connectorId).findFirst();
    }

    public long availableConnectors() {
        return online ? connectors.stream().filter(StationConnector::isAvailable).count() : 0;
    }

    public Optional<Double> distanceKmFrom(double lat, double lng) {
        if (latitude == null || longitude == null) {
            return Optional.empty();
        }
        return Optional.of(GeoDistance.kilometres(lat, lng, latitude.doubleValue(), longitude.doubleValue()));
    }
}
