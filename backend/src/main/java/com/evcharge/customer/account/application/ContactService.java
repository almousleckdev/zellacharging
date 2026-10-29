package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.application.port.PendingEmailRepository;
import com.evcharge.customer.account.domain.Channel;
import com.evcharge.customer.account.domain.ContactPoint;
import com.evcharge.customer.account.domain.Customer;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class ContactService {

    private final ProfileService profiles;
    private final CustomerRepository customers;
    private final PendingEmailRepository pending;
    private final VerificationService verification;
    private final ContactNormalizer contacts;
    private final AccountCountries countries;
    private final Clock clock;

    public ContactService(ProfileService profiles, CustomerRepository customers, PendingEmailRepository pending,
                          VerificationService verification, ContactNormalizer contacts, AccountCountries countries,
                          Clock clock) {
        this.profiles = profiles;
        this.customers = customers;
        this.pending = pending;
        this.verification = verification;
        this.contacts = contacts;
        this.countries = countries;
        this.clock = clock;
    }

    @Transactional
    public void startEmail(UUID customerId, String rawEmail) {
        Customer customer = profiles.get(customerId);
        if (customer.email() != null) {
            throw conflict("EMAIL_ALREADY_SET");
        }
        ContactPoint email = new ContactPoint(Channel.EMAIL, contacts.email(rawEmail));
        requireUnused(email);
        pending.save(customerId, email.target(), clock.instant());
        verification.reissue(customer, email);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public void confirmEmail(UUID customerId, String code) {
        Customer customer = profiles.get(customerId);
        String email = pending.find(customerId).orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                "NO_PENDING_EMAIL", "No email is waiting for confirmation"));
        verification.confirm(customer, Channel.EMAIL, code);
        customers.attachEmail(customerId, email, clock.instant());
        pending.delete(customerId);
    }

    @Transactional
    public void addPhone(UUID customerId, String rawPhone) {
        Customer customer = profiles.get(customerId);
        if (customer.phone() != null) {
            throw conflict("PHONE_ALREADY_SET");
        }
        ContactPoint phone = new ContactPoint(Channel.PHONE, contacts.phone(rawPhone));
        if (!countries.forTarget(Channel.PHONE, phone.target()).equals(TenantContext.require())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PHONE_COUNTRY_MISMATCH",
                    "The number does not belong to your country");
        }
        requireUnused(phone);
        customers.attachPhone(customerId, phone.target());
    }

    private void requireUnused(ContactPoint point) {
        if (customers.findByContact(point.channel(), point.target()).isPresent()) {
            throw conflict("CONTACT_ALREADY_USED");
        }
    }

    private static ApiException conflict(String code) {
        return new ApiException(HttpStatus.CONFLICT, code, "The contact cannot be added");
    }
}
