package com.evcharge.customer.admin.domain;

import java.util.Set;
import java.util.UUID;

public record AdminAccess(UUID id, String email, AdminRole role, Set<String> countries,
                          Set<Permission> permissions, boolean profileComplete) {

    public boolean can(Permission permission) {
        return permissions.contains(permission);
    }

    public boolean canSee(String country) {
        return countries.contains(country);
    }
}
