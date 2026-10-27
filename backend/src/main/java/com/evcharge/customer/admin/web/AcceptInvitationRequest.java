package com.evcharge.customer.admin.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record AcceptInvitationRequest(@NotBlank @Size(max = 200) String token, @NotBlank @Size(max = 128) String password) {
}
