package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.CountryEntry;
import com.evcharge.customer.charging.SessionDetail;

import java.math.BigDecimal;
import java.time.Instant;

record SessionDetailResponse(SessionListResponse.Item session, String idTag, Integer transactionId, String stopReason,
                             Long pricePerKwhMinor, BigDecimal lastVoltage, BigDecimal lastPowerKw,
                             BigDecimal lastEnergyKwh, Instant progressAt, Instant updatedAt, Long reservedMinor,
                             Long capturedMinor, String holdState, Long serviceFeeMinor, Long vatMinor) {

    static SessionDetailResponse from(CountryEntry<SessionDetail> entry) {
        SessionDetail d = entry.item();
        return new SessionDetailResponse(SessionListResponse.Item.from(new CountryEntry<>(entry.country(),
                d.record(), d.record().createdAt())), d.idTag(), d.transactionId(), d.stopReason(),
                d.pricePerKwhMinor(), d.lastVoltage(), d.lastPowerKw(), d.lastEnergyKwh(), d.progressAt(),
                d.updatedAt(), d.reservedMinor(), d.capturedMinor(), d.holdState(), d.serviceFeeMinor(), d.vatMinor());
    }
}
