package com.evcharge.customer.account.web;

import com.evcharge.customer.account.application.AccountCountries;
import com.evcharge.customer.account.application.LoginService;
import com.evcharge.customer.account.application.RegisterCommand;
import com.evcharge.customer.account.application.RegistrationService;
import com.evcharge.customer.account.application.RefreshSessionService;
import com.evcharge.customer.account.application.SocialLoginService;
import com.evcharge.customer.account.application.VerificationService;
import com.evcharge.customer.shared.TenantContext;
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
    private final SocialLoginService social;
    private final RefreshSessionService sessions;
    private final AccountCountries countries;

    AuthController(RegistrationService registration, VerificationService verification,
                   LoginService login, SocialLoginService social, RefreshSessionService sessions,
                   AccountCountries countries) {
        this.registration = registration;
        this.verification = verification;
        this.login = login;
        this.social = social;
        this.sessions = sessions;
        this.countries = countries;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return TenantContext.callAs(countries.forRegistration(request.email(), request.phone()),
                () -> new RegisterResponse(registration.register(new RegisterCommand(request.firstName(),
                        request.lastName(), request.email(), request.phone(), request.password()))));
    }

    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void verify(@Valid @RequestBody VerifyRequest request) {
        TenantContext.runAs(countries.forTarget(request.channel(), request.target()),
                () -> verification.verify(request.channel(), request.target(), request.code()));
    }

    @PostMapping("/verify/resend")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void resend(@Valid @RequestBody ResendRequest request) {
        TenantContext.runAs(countries.forTarget(request.channel(), request.target()),
                () -> verification.resend(request.channel(), request.target()));
    }

    @PostMapping("/login")
    TokensResponse login(@Valid @RequestBody LoginRequest request) {
        return TenantContext.callAs(countries.forIdentifier(request.identifier()),
                () -> TokensResponse.from(login.login(request.identifier(), request.password())));
    }

    @PostMapping("/social")
    TokensResponse social(@Valid @RequestBody SocialLoginRequest request) {
        return TenantContext.callAs(countries.forSocialSignIn(), () -> TokensResponse.from(
                social.signIn(request.provider(), request.idToken(), request.firstName(), request.lastName())));
    }

    @PostMapping("/refresh")
    TokensResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return TenantContext.callAs(countries.forRefreshToken(request.refreshToken()),
                () -> TokensResponse.from(sessions.refresh(request.refreshToken())));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(@Valid @RequestBody RefreshRequest request) {
        TenantContext.runAs(countries.forRefreshToken(request.refreshToken()),
                () -> sessions.logout(request.refreshToken()));
    }
}
