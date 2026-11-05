package com.evcharge.customer.charging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ChargeFeedItem(UUID sessionId, int transactionId, UUID customerId, String idTag, String stationId,
                             int connectorId, String sessionState, Instant startedAt, Instant stoppedAt,
                             BigDecimal energyKwh, Long energyCostMinor, Long vatMinor, Long serviceFeeMinor,
                             Long totalChargedMinor, String currency, Integer currencyDecimals,
                             String paymentState, boolean breakdownEstimated, Instant updatedAt) {
}
