package com.evcharge.customer.catalog.infrastructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
record OcppConnectorPayload(int connectorId, String status, String connectorType, BigDecimal maxPowerKw) {
}
