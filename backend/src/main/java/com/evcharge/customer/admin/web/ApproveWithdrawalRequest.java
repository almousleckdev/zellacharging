package com.evcharge.customer.admin.web;

import jakarta.validation.constraints.Size;

record ApproveWithdrawalRequest(@Size(max = 300) String note) {
}
