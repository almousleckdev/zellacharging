package com.evcharge.customer.account.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record AddPhoneRequest(@NotBlank @Size(max = 32) String phone) {
}
