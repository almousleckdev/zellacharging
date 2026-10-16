package com.evcharge.customer.wallet.web;

import com.evcharge.customer.wallet.domain.MobileOperator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

record TopupRequest(
        @Positive long amountMinor,
        @NotBlank String msisdn,
        @NotNull MobileOperator operator) {
}
