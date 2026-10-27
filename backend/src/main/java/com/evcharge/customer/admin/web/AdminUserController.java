package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminAccessService;
import com.evcharge.customer.admin.application.AdminLifecycleService;
import com.evcharge.customer.admin.application.AdminProfileService;
import com.evcharge.customer.admin.application.AdminUserService;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/users")
class AdminUserController {

    private final AdminUserService users;
    private final AdminLifecycleService lifecycle;
    private final AdminProfileService profiles;
    private final AdminAccessService access;

    AdminUserController(AdminUserService users, AdminLifecycleService lifecycle, AdminProfileService profiles,
                        AdminAccessService access) {
        this.users = users;
        this.lifecycle = lifecycle;
        this.profiles = profiles;
        this.access = access;
    }

    @GetMapping
    AdminUserPageResponse list(AdminAccess admin, @RequestParam(required = false) String q,
                               @RequestParam(required = false) String status,
                               @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return AdminUserPageResponse.from(users.list(admin, q, status, page, size));
    }

    @GetMapping("/{id}")
    AdminUserResponse get(AdminAccess admin, @PathVariable UUID id) {
        return AdminUserResponse.from(users.get(admin, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    InvitationResponse invite(AdminAccess admin, @Valid @RequestBody InviteAdminRequest request) {
        return InvitationResponse.from(users.invite(admin, request.toCommand()));
    }

    @PutMapping("/{id}")
    AdminUserResponse update(AdminAccess admin, @PathVariable UUID id, @Valid @RequestBody AccessRequest request) {
        return AdminUserResponse.from(users.update(admin, id, request.toCommand()));
    }

    @PostMapping("/{id}/invitation")
    InvitationEmailResponse resend(AdminAccess admin, @PathVariable UUID id) {
        return new InvitationEmailResponse(users.resendInvitation(admin, id));
    }

    @PostMapping("/{id}/disable")
    AdminUserResponse disable(AdminAccess admin, @PathVariable UUID id) {
        return AdminUserResponse.from(lifecycle.disable(admin, id));
    }

    @PostMapping("/{id}/enable")
    AdminUserResponse enable(AdminAccess admin, @PathVariable UUID id) {
        return AdminUserResponse.from(lifecycle.enable(admin, id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(AdminAccess admin, @PathVariable UUID id) {
        lifecycle.deleteInvited(admin, id);
    }

    @GetMapping("/{id}/avatar")
    ResponseEntity<byte[]> avatar(AdminAccess admin, @PathVariable UUID id) {
        access.require(admin, Permission.ADMINS_MANAGE);
        return profiles.avatar(id).map(AdminProfileController::image)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    record InvitationEmailResponse(boolean emailSent) {
    }
}
