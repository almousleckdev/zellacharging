package com.evcharge.customer.account.web;

import com.evcharge.customer.account.application.AccountClosureService;
import com.evcharge.customer.account.application.PasswordService;
import com.evcharge.customer.account.application.ProfileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

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
    ProfileResponse me(@AuthenticationPrincipal Jwt jwt) {
        return ProfileResponse.from(profiles.get(UUID.fromString(jwt.getSubject())));
    }

    @PatchMapping
    ProfileResponse update(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateProfileRequest request) {
        return ProfileResponse.from(profiles.updateName(UUID.fromString(jwt.getSubject()), request.firstName(),
                request.lastName()));
    }

    @PostMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void changePassword(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ChangePasswordRequest request) {
        passwords.change(UUID.fromString(jwt.getSubject()), request.currentPassword(), request.newPassword());
    }

    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteAccount(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DeleteAccountRequest request) {
        closure.close(UUID.fromString(jwt.getSubject()), request.password());
    }
}
