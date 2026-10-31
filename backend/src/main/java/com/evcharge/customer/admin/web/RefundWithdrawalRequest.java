package com.evcharge.customer.admin.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record RefundWithdrawalRequest(@NotBlank @Size(max = 300) String note) {
}
