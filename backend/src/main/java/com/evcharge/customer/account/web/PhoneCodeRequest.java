package com.evcharge.customer.account.web;

import com.evcharge.customer.account.domain.CodeDelivery;
import jakarta.validation.constraints.NotNull;

record PhoneCodeRequest(@NotNull CodeDelivery delivery) {
}
