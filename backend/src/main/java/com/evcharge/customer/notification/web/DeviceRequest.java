package com.evcharge.customer.notification.web;

import com.evcharge.customer.notification.domain.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

record DeviceRequest(@NotBlank @Size(max = 512) String token, @NotNull Platform platform,
                     @Pattern(regexp = "fr|en") String language) {
}
