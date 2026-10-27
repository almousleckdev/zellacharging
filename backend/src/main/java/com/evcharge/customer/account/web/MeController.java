package com.evcharge.customer.account.web;

import com.evcharge.customer.account.application.AccountClosureService;
import com.evcharge.customer.account.application.PasswordService;
import com.evcharge.customer.account.application.ProfileService;
import com.evcharge.customer.shared.CustomerPrincipal;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1/me")
class MeController {

    private final ProfileService profiles;
    private final PasswordService passwords;
    private final AccountClosureService closure;

    MeController(ProfileService profiles, PasswordService passwords, AccountClosureService closure) {
        this.profiles = profiles;
        this.passwords = passwords;
        this.closure = closure;
    }

    @GetMapping
    ProfileResponse me(CustomerPrincipal me) {
        return ProfileResponse.from(profiles.get(me.id()));
    }

    @PatchMapping
    ProfileResponse update(CustomerPrincipal me, @Valid @RequestBody UpdateProfileRequest request) {
        return ProfileResponse.from(profiles.updateName(me.id(), request.firstName(),
                request.lastName()));
    }

    @PostMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void changePassword(CustomerPrincipal me, @Valid @RequestBody ChangePasswordRequest request) {
        passwords.change(me.id(), request.currentPassword(), request.newPassword());
    }

    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteAccount(CustomerPrincipal me, @Valid @RequestBody DeleteAccountRequest request) {
        closure.close(me.id(), request.password());
    }
}
