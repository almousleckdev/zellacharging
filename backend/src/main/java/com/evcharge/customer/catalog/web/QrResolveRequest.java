package com.evcharge.customer.catalog.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record QrResolveRequest(@NotBlank @Size(max = 2048) String content) {
}
