package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.PhoneNumbers;
import com.evcharge.customer.account.domain.Channel;
import com.evcharge.customer.account.domain.ContactPoint;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ContactNormalizer {

    private final PhoneNumbers phones;

    public ContactNormalizer(PhoneNumbers phones) {
        this.phones = phones;
    }

    public String email(String raw) {
        return raw.trim().toLowerCase(Locale.ROOT);
    }

    public String phone(String raw) {
        return phones.toE164(raw);
    }

    public ContactPoint resolve(String identifier) {
        if (identifier.contains("@")) {
            return new ContactPoint(Channel.EMAIL, email(identifier));
        }
        return new ContactPoint(Channel.PHONE, phone(identifier));
    }

    public ContactPoint of(Channel channel, String target) {
        return channel == Channel.EMAIL
                ? new ContactPoint(channel, email(target))
                : new ContactPoint(channel, phone(target));
    }
}
