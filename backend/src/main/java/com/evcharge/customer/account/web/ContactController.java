package com.evcharge.customer.account.web;

import com.evcharge.customer.account.application.ContactService;
import com.evcharge.customer.account.application.ProfileService;
import com.evcharge.customer.shared.CustomerPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/me")
class ContactController {

    private final ContactService contacts;
    private final ProfileService profiles;

    ContactController(ContactService contacts, ProfileService profiles) {
        this.contacts = contacts;
        this.profiles = profiles;
    }

    @PostMapping("/email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void addEmail(CustomerPrincipal me, @Valid @RequestBody AddEmailRequest request) {
        contacts.startEmail(me.id(), request.email());
    }

    @PostMapping("/email/verify")
    ProfileResponse confirmEmail(CustomerPrincipal me, @Valid @RequestBody ConfirmCodeRequest request) {
        contacts.confirmEmail(me.id(), request.code());
        return ProfileResponse.from(profiles.view(me.id()));
    }

    @PostMapping("/phone")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void addPhone(CustomerPrincipal me, @Valid @RequestBody AddPhoneRequest request) {
        contacts.startPhone(me.id(), request.phone(), request.delivery());
    }

    @PostMapping("/phone/code")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void sendPhoneCode(CustomerPrincipal me, @Valid @RequestBody PhoneCodeRequest request) {
        contacts.sendPhoneCode(me.id(), request.delivery());
    }

    @PostMapping("/phone/verify")
    ProfileResponse confirmPhone(CustomerPrincipal me, @Valid @RequestBody ConfirmCodeRequest request) {
        contacts.confirmPhone(me.id(), request.code());
        return ProfileResponse.from(profiles.view(me.id()));
    }
}
