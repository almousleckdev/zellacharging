package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.CountryEntry;
import com.evcharge.customer.wallet.PaymentDetail;

import java.time.Instant;

record PaymentDetailResponse(PaymentListResponse.Item payment, String providerRef, Instant updatedAt) {

    static PaymentDetailResponse from(CountryEntry<PaymentDetail> entry) {
        PaymentDetail d = entry.item();
        return new PaymentDetailResponse(PaymentListResponse.Item.from(new CountryEntry<>(entry.country(),
                d.record(), d.record().createdAt())), d.providerRef(), d.updatedAt());
    }
}
