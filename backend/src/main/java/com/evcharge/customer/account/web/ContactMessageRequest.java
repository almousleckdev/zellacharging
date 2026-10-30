package com.evcharge.customer.account.web;

import com.evcharge.customer.account.domain.ContactMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record ContactMessageRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email @Size(max = 200) String email,
        @Size(max = 40) String phone,
        @NotBlank @Size(max = 60) String topic,
        @NotBlank @Size(min = 10, max = 4000) String message) {

    ContactMessage toMessage() {
        return new ContactMessage(name.trim(), email.trim(), phone == null || phone.isBlank() ? null : phone.trim(),
                topic.trim(), message.trim());
    }
}
