package com.evcharge.customer.account.web;

import com.evcharge.customer.account.domain.SocialProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record SocialLoginRequest(
        @NotNull SocialProvider provider,
        @NotBlank @Size(max = 4096) String idToken,
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName) {
}
