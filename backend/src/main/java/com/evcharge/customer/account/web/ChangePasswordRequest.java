package com.evcharge.customer.account.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record ChangePasswordRequest(@NotBlank String currentPassword, @NotBlank @Size(min = 8, max = 128) String newPassword) {
}
