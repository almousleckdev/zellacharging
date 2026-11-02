package com.evcharge.customer.admin.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record ForgotPasswordRequest(@NotBlank @Size(max = 254) String email) {
}
