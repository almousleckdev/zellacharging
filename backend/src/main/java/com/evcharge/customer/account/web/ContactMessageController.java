package com.evcharge.customer.account.web;

import com.evcharge.customer.account.application.ContactMessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/contact")
class ContactMessageController {

    private final ContactMessageService messages;

    ContactMessageController(ContactMessageService messages) {
        this.messages = messages;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    void send(@Valid @RequestBody ContactMessageRequest request) {
        messages.send(request.toMessage());
    }
}
