package com.evcharge.customer.admin.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record ChangeAdminPasswordRequest(@NotBlank @Size(max = 128) String currentPassword,
                                  @NotBlank @Size(max = 128) String newPassword) {
}
