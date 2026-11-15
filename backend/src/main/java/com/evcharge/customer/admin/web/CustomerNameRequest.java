package com.evcharge.customer.admin.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record CustomerNameRequest(@NotBlank @Size(max = 100) String firstName, @NotBlank @Size(max = 100) String lastName) {
}
