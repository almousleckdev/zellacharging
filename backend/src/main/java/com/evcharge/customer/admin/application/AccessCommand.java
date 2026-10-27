package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.admin.domain.Permission;

import java.util.Set;

public record AccessCommand(AdminRole role, Set<Permission> permissions, Set<String> countries) {
}
