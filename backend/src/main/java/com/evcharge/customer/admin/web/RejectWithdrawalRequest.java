package com.evcharge.customer.admin.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record RejectWithdrawalRequest(@NotBlank @Size(max = 300) String note) {
}
