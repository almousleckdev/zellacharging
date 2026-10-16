package com.evcharge.customer.catalog.application.port;

import com.evcharge.customer.catalog.domain.ConnectorRef;
import com.evcharge.customer.catalog.domain.Station;

import java.util.List;
import java.util.Optional;

public interface StationDirectory {

    List<Station> list(Double latitude, Double longitude, Double radiusKm);

    Optional<Station> find(String stationId);

    Optional<ConnectorRef> resolveQr(String token);
}
