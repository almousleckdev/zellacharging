package com.evcharge.customer.catalog;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record StationInventory(String id, String isoCode, boolean online, String vendor, String model,
                               Instant lastSeenAt, BigDecimal latitude, BigDecimal longitude,
                               List<InventoryConnector> connectors) {
}
