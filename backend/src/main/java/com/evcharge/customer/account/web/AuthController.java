package com.evcharge.customer.account.web;

import com.evcharge.customer.account.application.LoginService;
import com.evcharge.customer.account.application.RegisterCommand;
import com.evcharge.customer.account.application.RegistrationService;
import com.evcharge.customer.account.application.RefreshSessionService;
import com.evcharge.customer.account.application.VerificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
class AuthController {

    private final RegistrationService registration;
    private final VerificationService verification;
    private final LoginService login;
    private final RefreshSessionService sessions;

    AuthController(RegistrationService registration, VerificationService verification,
                   LoginService login, RefreshSessionService sessions) {
        this.registration = registration;
        this.verification = verification;
        this.login = login;
        this.sessions = sessions;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return new RegisterResponse(registration.register(new RegisterCommand(request.firstName(),
                request.lastName(), request.email(), request.phone(), request.password())));
    }

    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void verify(@Valid @RequestBody VerifyRequest request) {
        verification.verify(request.channel(), request.target(), request.code());
    }

    @PostMapping("/verify/resend")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void resend(@Valid @RequestBody ResendRequest request) {
        verification.resend(request.channel(), request.target());
    }

    @PostMapping("/login")
    TokensResponse login(@Valid @RequestBody LoginRequest request) {
        return TokensResponse.from(login.login(request.identifier(), request.password()));
    }

    @PostMapping("/refresh")
    TokensResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return TokensResponse.from(sessions.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(@Valid @RequestBody RefreshRequest request) {
        sessions.logout(request.refreshToken());
    }
}
