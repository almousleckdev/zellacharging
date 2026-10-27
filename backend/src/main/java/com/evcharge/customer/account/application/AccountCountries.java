package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.PhoneNumbers;
import com.evcharge.customer.account.domain.Channel;
import com.evcharge.customer.account.domain.ContactPoint;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.Country;
import com.evcharge.customer.shared.CountryDirectory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AccountCountries {

    private final CountryDirectory directory;
    private final ContactNormalizer contacts;
    private final PhoneNumbers phones;

    public AccountCountries(CountryDirectory directory, ContactNormalizer contacts, PhoneNumbers phones) {
        this.directory = directory;
        this.contacts = contacts;
        this.phones = phones;
    }

    public String forRegistration(String email, String phone) {
        if (phone != null && !phone.isBlank()) {
            return forContact(new ContactPoint(Channel.PHONE, contacts.phone(phone)));
        }
        return directory.defaultCountry().code();
    }

    public String forIdentifier(String identifier) {
        return forContact(contacts.resolve(identifier));
    }

    public String forTarget(Channel channel, String target) {
        return forContact(contacts.of(channel, target));
    }

    public String forRefreshToken(String rawToken) {
        return RefreshTokenFormat.countryOf(rawToken).flatMap(directory::activeByCode).map(Country::code)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN",
                        "Invalid refresh token"));
    }

    private String forContact(ContactPoint point) {
        if (point.channel() == Channel.EMAIL) {
            return directory.defaultCountry().code();
        }
        return directory.activeByPhoneRegion(phones.regionOf(point.target())).map(Country::code)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "UNSUPPORTED_COUNTRY",
                        "This country is not supported"));
    }
}
