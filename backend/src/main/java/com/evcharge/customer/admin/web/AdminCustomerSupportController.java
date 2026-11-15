package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminCustomerSupportService;
import com.evcharge.customer.admin.domain.AdminAccess;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/customers/{country}/{id}")
class AdminCustomerSupportController {

    private final AdminCustomerSupportService support;

    AdminCustomerSupportController(AdminCustomerSupportService support) {
        this.support = support;
    }

    @PostMapping("/resend-code")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void resendCode(AdminAccess admin, @PathVariable String country, @PathVariable UUID id,
                    @Valid @RequestBody CustomerChannelRequest request) {
        support.resendCode(admin, country, id, request.channel(), request.delivery());
    }

    @PostMapping("/reset-attempts")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void resetAttempts(AdminAccess admin, @PathVariable String country, @PathVariable UUID id,
                       @Valid @RequestBody CustomerChannelRequest request) {
        support.resetCodeAttempts(admin, country, id, request.channel());
    }

    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void verify(AdminAccess admin, @PathVariable String country, @PathVariable UUID id,
                @Valid @RequestBody CustomerChannelRequest request) {
        support.markVerified(admin, country, id, request.channel());
    }

    @PostMapping("/unlock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void unlock(AdminAccess admin, @PathVariable String country, @PathVariable UUID id) {
        support.unlockLogin(admin, country, id);
    }

    @PutMapping("/contact")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void changeContact(AdminAccess admin, @PathVariable String country, @PathVariable UUID id,
                       @Valid @RequestBody CustomerContactRequest request) {
        support.changeContact(admin, country, id, request.channel(), request.value());
    }

    @PutMapping("/ndzela-link")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void link(AdminAccess admin, @PathVariable String country, @PathVariable UUID id) {
        support.linkNdzela(admin, country, id);
    }

    @DeleteMapping("/ndzela-link")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void unlink(AdminAccess admin, @PathVariable String country, @PathVariable UUID id) {
        support.unlinkNdzela(admin, country, id);
    }
}
