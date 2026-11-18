package com.evcharge.customer.partner.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record StaffAccessRequest(@NotBlank @Size(max = 200) String reason) {
}
