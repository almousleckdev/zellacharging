package com.evcharge.customer.account.web;

import jakarta.validation.constraints.NotBlank;

record DeleteAccountRequest(@NotBlank String password) {
}
