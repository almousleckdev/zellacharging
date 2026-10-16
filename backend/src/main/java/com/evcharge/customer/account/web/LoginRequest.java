package com.evcharge.customer.account.web;

import jakarta.validation.constraints.NotBlank;

record LoginRequest(@NotBlank String identifier, @NotBlank String password) {
}
