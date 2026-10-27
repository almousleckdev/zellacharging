package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AccessCommand;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.admin.domain.Permission;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

record AccessRequest(@NotNull AdminRole role, Set<Permission> permissions, Set<String> countries) {

    AccessCommand toCommand() {
        return new AccessCommand(role, permissions, countries);
    }
}
