package com.evcharge.customer.catalog.application;

import com.evcharge.customer.catalog.domain.Station;
import com.evcharge.customer.catalog.domain.StationConnector;

public record ResolvedConnector(Station station, StationConnector connector) {
}
