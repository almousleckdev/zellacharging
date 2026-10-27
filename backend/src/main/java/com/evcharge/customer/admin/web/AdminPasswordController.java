package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminPasswordService;
import com.evcharge.customer.admin.domain.AdminAccess;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/me/password")
class AdminPasswordController {

    private final AdminPasswordService passwords;

    AdminPasswordController(AdminPasswordService passwords) {
        this.passwords = passwords;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void change(AdminAccess admin, @Valid @RequestBody ChangeAdminPasswordRequest request) {
        passwords.change(admin.id(), request.currentPassword(), request.newPassword());
    }
}
