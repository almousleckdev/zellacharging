package com.evcharge.customer.catalog;

import java.util.List;
import java.util.Optional;

public interface StationInventories {

    List<StationInventory> all();

    Optional<StationInventory> find(String stationId);
}
