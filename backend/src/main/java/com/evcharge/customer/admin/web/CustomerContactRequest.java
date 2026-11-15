package com.evcharge.customer.admin.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

record CustomerContactRequest(@NotBlank @Pattern(regexp = "EMAIL|PHONE") String channel,
                              @NotBlank @Size(max = 254) String value) {
}
