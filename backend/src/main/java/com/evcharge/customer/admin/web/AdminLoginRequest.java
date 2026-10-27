package com.evcharge.customer.admin.web;

import jakarta.validation.constraints.NotBlank;

record AdminLoginRequest(@NotBlank String email, @NotBlank String password) {
}
