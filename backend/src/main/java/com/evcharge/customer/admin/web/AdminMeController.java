package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminAccessService;
import com.evcharge.customer.admin.application.AdminProfileService;
import com.evcharge.customer.admin.domain.AdminAccess;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/me")
class AdminMeController {

    private final AdminAccessService access;
    private final AdminProfileService profiles;

    AdminMeController(AdminAccessService access, AdminProfileService profiles) {
        this.access = access;
        this.profiles = profiles;
    }

    @GetMapping
    AdminMeResponse me(AdminAccess admin) {
        return AdminMeResponse.of(admin, access.visibleCountries(admin), profiles.current(admin.id()));
    }
}
