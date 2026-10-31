package com.evcharge.customer.account.application.port;

import com.evcharge.customer.account.domain.ContactMessage;

public interface ContactInbox {

    boolean deliver(ContactMessage message);
}
