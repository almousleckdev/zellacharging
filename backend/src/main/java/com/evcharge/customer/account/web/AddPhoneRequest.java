package com.evcharge.customer.account.web;

import com.evcharge.customer.account.domain.CodeDelivery;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record AddPhoneRequest(@NotBlank @Size(max = 32) String phone, @NotNull CodeDelivery delivery) {
}
