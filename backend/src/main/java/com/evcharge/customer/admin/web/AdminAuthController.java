package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminLoginService;
import com.evcharge.customer.admin.application.AdminRefreshService;
import com.evcharge.customer.admin.application.AdminTotpService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/auth")
class AdminAuthController {

    private final AdminLoginService login;
    private final AdminTotpService totp;
    private final AdminRefreshService sessions;

    AdminAuthController(AdminLoginService login, AdminTotpService totp, AdminRefreshService sessions) {
        this.login = login;
        this.totp = totp;
        this.sessions = sessions;
    }

    @PostMapping("/login")
    AdminLoginResponse login(@Valid @RequestBody AdminLoginRequest request) {
        return AdminLoginResponse.from(login.login(request.email(), request.password()));
    }

    @PostMapping("/totp")
    AdminTokensResponse totp(@Valid @RequestBody AdminTotpRequest request) {
        return AdminTokensResponse.from(totp.complete(request.challengeToken(), request.code()));
    }

    @PostMapping("/refresh")
    AdminTokensResponse refresh(@Valid @RequestBody AdminRefreshRequest request) {
        return AdminTokensResponse.from(sessions.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(@Valid @RequestBody AdminRefreshRequest request) {
        sessions.logout(request.refreshToken());
    }
}
