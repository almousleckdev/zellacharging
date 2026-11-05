package com.evcharge.customer.partner.web;

import com.evcharge.customer.charging.ChargeFeedItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

record FeedItemView(int transactionId, UUID customerId, String idTag, String stationId, int connectorId,
                    String sessionState, Instant startedAt, Instant stoppedAt, BigDecimal energyKwh,
                    Long energyCostMinor, Long vatMinor, Long serviceFeeMinor, Long totalChargedMinor,
                    String currency, Integer currencyDecimals, String paymentMethod, String paymentState,
                    boolean breakdownEstimated, Instant updatedAt) {

    private static final String WALLET = "WALLET";

    static FeedItemView of(ChargeFeedItem item) {
        return new FeedItemView(item.transactionId(), item.customerId(), item.idTag(), item.stationId(),
                item.connectorId(), item.sessionState(), item.startedAt(), item.stoppedAt(), item.energyKwh(),
                item.energyCostMinor(), item.vatMinor(), item.serviceFeeMinor(), item.totalChargedMinor(),
                item.currency(), item.currencyDecimals(), WALLET, item.paymentState(), item.breakdownEstimated(),
                item.updatedAt());
    }
}
