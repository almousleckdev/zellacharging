package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.ManagedAdmin;
import com.evcharge.customer.admin.domain.Permission;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

record AdminUserResponse(UUID id, String email, String firstName, String lastName, String phone, String role,
                         String status, List<String> permissions, List<String> countries, Instant lastLoginAt,
                         Long avatarVersion) {

    static AdminUserResponse from(ManagedAdmin managed) {
        var user = managed.user();
        return new AdminUserResponse(user.id(), user.email(), user.firstName(), user.lastName(), user.phone(),
                user.role().name(), user.status().name(),
                user.effectivePermissions().stream().map(Permission::name).sorted().toList(),
                user.grantedCountries().stream().sorted().toList(), user.lastLoginAt(),
                managed.avatarUpdatedAt() == null ? null : managed.avatarUpdatedAt().toEpochMilli());
    }
}
