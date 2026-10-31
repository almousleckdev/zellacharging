package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.ContactInbox;
import com.evcharge.customer.account.domain.ContactMessage;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ContactMessageService {

    private final ContactInbox inbox;

    public ContactMessageService(ContactInbox inbox) {
        this.inbox = inbox;
    }

    public void send(ContactMessage message) {
        if (!inbox.deliver(message)) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "CONTACT_UNAVAILABLE",
                    "The message could not be delivered");
        }
    }
}
