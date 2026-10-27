package com.evcharge.customer.admin.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record UpdateProfileRequest(@NotBlank @Size(max = 60) String firstName, @NotBlank @Size(max = 60) String lastName,
                            @Size(max = 32) String phone) {
}
