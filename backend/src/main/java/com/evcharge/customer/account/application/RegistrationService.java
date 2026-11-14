package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.application.port.PasswordHasher;
import com.evcharge.customer.account.domain.Channel;
import com.evcharge.customer.account.domain.CodeDelivery;
import com.evcharge.customer.account.domain.ContactPoint;
import com.evcharge.customer.account.domain.Customer;
import com.evcharge.customer.account.domain.CustomerStatus;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class RegistrationService {

    private final CustomerRepository customers;
    private final PasswordHasher passwords;
    private final ContactNormalizer contacts;
    private final VerificationService verification;
    private final Clock clock;

    public RegistrationService(CustomerRepository customers, PasswordHasher passwords,
                               ContactNormalizer contacts, VerificationService verification, Clock clock) {
        this.customers = customers;
        this.passwords = passwords;
        this.contacts = contacts;
        this.verification = verification;
        this.clock = clock;
    }

    @Transactional
    public RegisteredAccount register(RegisterCommand command) {
        ContactPoint email = new ContactPoint(Channel.EMAIL, contacts.email(required(command.email())));
        ContactPoint phone = new ContactPoint(Channel.PHONE, contacts.phone(required(command.phone())));
        requireUnused(email);
        requireUnused(phone);
        Customer customer = new Customer(UUID.randomUUID(), TenantContext.require(), command.firstName().trim(),
                command.lastName().trim(), email.target(), phone.target(), passwords.hash(command.password()),
                CustomerStatus.PENDING_VERIFICATION, 0, null, clock.instant());
        customers.save(customer);
        verification.issue(customer, phone, phone.channel(), deliveryFor(phone, command.delivery()));
        verification.issue(customer, email, email.channel(), CodeDelivery.EMAIL);
        return new RegisteredAccount(customer.id(), phone);
    }

    static CodeDelivery deliveryFor(ContactPoint contact, CodeDelivery requested) {
        if (contact.channel() == Channel.EMAIL) {
            return CodeDelivery.EMAIL;
        }
        return requested != null && requested.isPhone() ? requested : CodeDelivery.WHATSAPP;
    }

    private void requireUnused(ContactPoint point) {
        if (customers.findByContact(point.channel(), point.target()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "CONTACT_ALREADY_USED", "Contact already registered");
        }
    }

    private static String required(String value) {
        if (value == null || value.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CONTACT_REQUIRED",
                    "An email and a phone number are required");
        }
        return value;
    }
}
