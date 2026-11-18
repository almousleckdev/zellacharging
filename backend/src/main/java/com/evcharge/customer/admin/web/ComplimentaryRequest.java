package com.evcharge.customer.admin.web;

import com.evcharge.customer.account.ComplimentaryCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

record ComplimentaryRequest(@NotBlank @Size(max = 200) String reason, ComplimentaryCategory category,
                            Instant expiresAt) {
}
