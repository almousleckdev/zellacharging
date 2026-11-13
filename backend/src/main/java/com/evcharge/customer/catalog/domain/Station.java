package com.evcharge.customer.catalog.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;

public record Station(
        String id,
        String isoCode,
        String vendor,
        String model,
        boolean online,
        Instant lastSeenAt,
        BigDecimal latitude,
        BigDecimal longitude,
        List<StationConnector> connectors) {

    public Optional<StationConnector> connector(int connectorId) {
        return connectors.stream().filter(c -> c.connectorId() == connectorId).findFirst();
    }

    public Station occupying(Set<Integer> busy, Map<Integer, Integer> minutesLeft) {
        if (busy == null || busy.isEmpty()) {
            return this;
        }
        Map<Integer, Integer> minutes = minutesLeft == null ? Map.of() : minutesLeft;
        List<StationConnector> marked = connectors.stream()
                .map(c -> busy.contains(c.connectorId()) ? c.inUse(minutes.get(c.connectorId())) : c).toList();
        return new Station(id, isoCode, vendor, model, online, lastSeenAt, latitude, longitude, marked);
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
