package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.application.port.PendingEmailRepository;
import com.evcharge.customer.account.application.port.PendingPhoneRepository;
import com.evcharge.customer.account.application.port.RefreshTokenRepository;
import com.evcharge.customer.account.domain.Channel;
import com.evcharge.customer.account.domain.ContactPoint;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

@Component
class ContactChange {

    private final CustomerRepository customers;
    private final PendingEmailRepository pendingEmails;
    private final PendingPhoneRepository pendingPhones;
    private final RefreshTokenRepository refreshTokens;
    private final ContactNormalizer contacts;
    private final AccountCountries countries;
    private final Clock clock;

    ContactChange(CustomerRepository customers, PendingEmailRepository pendingEmails,
                  PendingPhoneRepository pendingPhones, RefreshTokenRepository refreshTokens,
                  ContactNormalizer contacts, AccountCountries countries, Clock clock) {
        this.customers = customers;
        this.pendingEmails = pendingEmails;
        this.pendingPhones = pendingPhones;
        this.refreshTokens = refreshTokens;
        this.contacts = contacts;
        this.countries = countries;
        this.clock = clock;
    }

    void apply(UUID customerId, Channel channel, String rawValue) {
        ContactPoint point = contacts.of(channel, rawValue);
        validate(point, customerId);
        customers.replaceContact(customerId, channel, point.target());
        if (channel == Channel.EMAIL) {
            pendingEmails.delete(customerId);
        } else {
            pendingPhones.delete(customerId);
        }
        refreshTokens.revokeAllOf(customerId, clock.instant());
    }

    void validate(ContactPoint point, UUID except) {
        requireRightCountry(point);
        customers.findByContact(point.channel(), point.target())
                .filter(owner -> !owner.id().equals(except))
                .ifPresent(owner -> {
                    throw new ApiException(HttpStatus.CONFLICT, "CONTACT_ALREADY_USED",
                            "The contact cannot be added");
                });
    }

    private void requireRightCountry(ContactPoint point) {
        if (point.channel() == Channel.PHONE
                && !countries.forTarget(Channel.PHONE, point.target()).equals(TenantContext.require())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PHONE_COUNTRY_MISMATCH",
                    "The number does not belong to this country");
        }
    }
}
