package com.evcharge.customer.account.web;

import com.evcharge.customer.account.application.AccountCountries;
import com.evcharge.customer.account.application.PasswordResetService;
import com.evcharge.customer.shared.TenantContext;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth/password")
class PasswordResetController {

    private final PasswordResetService resets;
    private final AccountCountries countries;

    PasswordResetController(PasswordResetService resets, AccountCountries countries) {
        this.resets = resets;
        this.countries = countries;
    }

    @PostMapping("/forgot")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void forgot(@Valid @RequestBody ForgotPasswordRequest request) {
        TenantContext.runAs(countries.forIdentifier(request.contact()),
                () -> resets.request(request.contact(), request.delivery()));
    }

    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void reset(@Valid @RequestBody ResetPasswordRequest request) {
        TenantContext.runAs(countries.forIdentifier(request.contact()),
                () -> resets.reset(request.contact(), request.code(), request.newPassword()));
    }
}
