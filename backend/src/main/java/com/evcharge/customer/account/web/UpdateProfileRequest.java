package com.evcharge.customer.account.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record UpdateProfileRequest(@NotBlank @Size(max = 100) String firstName, @NotBlank @Size(max = 100) String lastName) {
}
