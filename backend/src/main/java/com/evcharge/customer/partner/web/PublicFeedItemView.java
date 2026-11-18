package com.evcharge.customer.partner.web;

import com.evcharge.customer.charging.ChargeFeedItem;

import java.math.BigDecimal;
import java.time.Instant;

// A session of someone who is not the partner's own driver: what was charged, never who
record PublicFeedItemView(int transactionId, String stationId, int connectorId, String sessionState,
                          Instant startedAt, Instant stoppedAt, BigDecimal energyKwh, Long energyCostMinor,
                          Long vatMinor, Long serviceFeeMinor, Long totalChargedMinor, String currency,
                          Integer currencyDecimals, String paymentState, boolean breakdownEstimated,
                          BigDecimal batteryStartPercent, BigDecimal batteryPercent, BigDecimal powerKw,
                          Long pricePerKwhMinor, Instant updatedAt, String freeCategory) {

    static PublicFeedItemView of(ChargeFeedItem item) {
        return new PublicFeedItemView(item.transactionId(), item.stationId(), item.connectorId(),
                item.sessionState(), item.startedAt(), item.stoppedAt(), item.energyKwh(), item.energyCostMinor(),
                item.vatMinor(), item.serviceFeeMinor(), item.totalChargedMinor(), item.currency(),
                item.currencyDecimals(), item.paymentState(), item.breakdownEstimated(), item.batteryStartPercent(),
                item.batteryPercent(), item.powerKw(), item.pricePerKwhMinor(), item.updatedAt(),
                item.freeCategory());
    }
}
