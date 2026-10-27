package com.evcharge.customer.wallet.web;

import com.evcharge.customer.wallet.domain.MobileOperator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

record WithdrawalRequest(
        @Positive long amountMinor,
        @NotNull MobileOperator operator,
        @NotBlank @Size(max = 72) String password) {
}
