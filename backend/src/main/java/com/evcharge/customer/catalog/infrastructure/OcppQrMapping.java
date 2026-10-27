package com.evcharge.customer.catalog.infrastructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
record OcppQrMapping(String deviceId, int connectorId) {
}
