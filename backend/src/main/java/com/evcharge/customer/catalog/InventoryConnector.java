package com.evcharge.customer.catalog;

import java.math.BigDecimal;

public record InventoryConnector(int connectorId, String status, String connectorType, BigDecimal maxPowerKw) {
}
