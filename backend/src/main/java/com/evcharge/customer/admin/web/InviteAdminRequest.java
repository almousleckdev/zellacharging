package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.InviteCommand;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.admin.domain.Permission;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

record InviteAdminRequest(@NotBlank @Email @Size(max = 254) String email, @NotNull AdminRole role,
                          Set<Permission> permissions, Set<String> countries) {

    InviteCommand toCommand() {
        return new InviteCommand(email, new AccessRequest(role, permissions, countries).toCommand());
    }
}
