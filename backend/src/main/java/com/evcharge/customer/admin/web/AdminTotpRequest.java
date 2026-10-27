package com.evcharge.customer.admin.web;

import jakarta.validation.constraints.NotBlank;

record AdminTotpRequest(@NotBlank String challengeToken, @NotBlank String code) {
}
