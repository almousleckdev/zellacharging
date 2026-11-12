package com.evcharge.customer.admin.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

record ComplimentaryRequest(@NotBlank @Size(max = 200) String reason, Instant expiresAt) {
}
