package com.evcharge.customer.admin.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

record IssueKeyRequest(@NotNull @Pattern(regexp = "^[A-Z]{2}$") String country) {
}
