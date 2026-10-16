package com.evcharge.customer.charging.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

record StartSessionRequest(@NotBlank String stationId, @Min(1) int connectorId, @Positive long amountMinor) {
}
