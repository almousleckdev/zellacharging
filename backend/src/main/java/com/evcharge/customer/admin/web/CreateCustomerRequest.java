package com.evcharge.customer.admin.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record CreateCustomerRequest(@NotBlank @Size(max = 80) String firstName, @NotBlank @Size(max = 80) String lastName,
                             @Size(max = 254) String email, @Size(max = 32) String phone) {
}
