package com.evcharge.customer.admin.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record ReviewIncidentRequest(@NotBlank String key, @NotBlank String status, @Size(max = 500) String note) {
}
