package com.evcharge.customer.admin.web;

import jakarta.validation.constraints.NotBlank;

record AdminRefreshRequest(@NotBlank String refreshToken) {
}
