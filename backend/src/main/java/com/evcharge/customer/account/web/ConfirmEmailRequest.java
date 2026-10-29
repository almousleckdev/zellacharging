package com.evcharge.customer.account.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

record ConfirmEmailRequest(@NotBlank @Pattern(regexp = "\\d{6}") String code) {
}
