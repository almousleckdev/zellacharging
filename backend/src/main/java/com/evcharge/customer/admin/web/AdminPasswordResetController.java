package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminPasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/auth/password")
class AdminPasswordResetController {

    private final AdminPasswordResetService resets;

    AdminPasswordResetController(AdminPasswordResetService resets) {
        this.resets = resets;
    }

    @PostMapping("/forgot")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void forgot(@Valid @RequestBody ForgotPasswordRequest request) {
        resets.request(request.email());
    }

    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void reset(@Valid @RequestBody ResetPasswordRequest request) {
        resets.reset(request.token(), request.password());
    }
}
