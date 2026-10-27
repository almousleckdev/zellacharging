package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.AdminAvatarRepository;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.admin.domain.AdminUser;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class AdminTargets {

    private final AdminRepository admins;
    private final AdminAvatarRepository avatars;

    AdminTargets(AdminRepository admins, AdminAvatarRepository avatars) {
        this.admins = admins;
        this.avatars = avatars;
    }

    AdminUser find(UUID id) {
        return admins.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ADMIN_NOT_FOUND",
                "Administrator not found"));
    }

    AdminUser unprotected(AdminAccess actor, UUID id) {
        AdminUser target = find(id);
        if (target.role() == AdminRole.SUPER_ADMIN || target.id().equals(actor.id())) {
            throw new ApiException(HttpStatus.CONFLICT, "ADMIN_PROTECTED", "This account cannot be modified here");
        }
        return target;
    }

    ManagedAdmin managed(AdminUser user) {
        return new ManagedAdmin(user, avatars.updatedAt(user.id()).orElse(null));
    }
}
