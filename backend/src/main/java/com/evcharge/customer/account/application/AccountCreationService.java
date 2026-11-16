package com.evcharge.customer.account.application;

import com.evcharge.customer.account.CustomerCreation;
import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.application.port.PasswordHasher;
import com.evcharge.customer.account.domain.Channel;
import com.evcharge.customer.account.domain.ContactPoint;
import com.evcharge.customer.account.domain.Customer;
import com.evcharge.customer.account.domain.CustomerStatus;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
class AccountCreationService implements CustomerCreation {

    private final SecureRandom random = new SecureRandom();
    private final CustomerRepository customers;
    private final PasswordHasher passwords;
    private final ContactNormalizer contacts;
    private final ContactChange change;
    private final Clock clock;

    AccountCreationService(CustomerRepository customers, PasswordHasher passwords, ContactNormalizer contacts,
                           ContactChange change, Clock clock) {
        this.customers = customers;
        this.passwords = passwords;
        this.contacts = contacts;
        this.change = change;
        this.clock = clock;
    }

    @Override
    @Transactional
    public UUID create(String firstName, String lastName, String email, String phone) {
        List<ContactPoint> points = Stream.of(pointOf(Channel.EMAIL, email), pointOf(Channel.PHONE, phone))
                .filter(point -> point != null).toList();
        if (points.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CONTACT_REQUIRED",
                    "An email or a phone number is required");
        }
        points.forEach(point -> change.validate(point, null));
        Instant now = clock.instant();
        String unusableHash = passwords.hash(unusablePassword());
        Customer customer = new Customer(UUID.randomUUID(), TenantContext.require(), firstName.trim(),
                lastName.trim(), target(points, Channel.EMAIL), target(points, Channel.PHONE),
                unusableHash, CustomerStatus.PENDING_VERIFICATION, 0, null, now);
        customers.save(customer);
        customers.markPasswordless(customer.id(), unusableHash);
        points.forEach(point -> customers.markContactVerified(customer.id(), point.channel(), now));
        return customer.id();
    }

    private ContactPoint pointOf(Channel channel, String raw) {
        return raw == null || raw.isBlank() ? null : contacts.of(channel, raw);
    }

    private static String target(List<ContactPoint> points, Channel channel) {
        return points.stream().filter(point -> point.channel() == channel).map(ContactPoint::target).findFirst()
                .orElse(null);
    }

    private String unusablePassword() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
