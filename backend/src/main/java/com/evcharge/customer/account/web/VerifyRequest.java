package com.evcharge.customer.account.web;

import com.evcharge.customer.account.domain.Channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

record VerifyRequest(
        @NotNull Channel channel,
        @NotBlank String target,
        @NotBlank @Pattern(regexp = "\\d{6}") String code) {
}
