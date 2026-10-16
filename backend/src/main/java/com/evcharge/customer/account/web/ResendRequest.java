package com.evcharge.customer.account.web;

import com.evcharge.customer.account.domain.Channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

record ResendRequest(@NotNull Channel channel, @NotBlank String target) {
}
