package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.application.port.PasswordHasher;
import com.evcharge.customer.account.domain.Channel;
import com.evcharge.customer.account.domain.ContactPoint;
import com.evcharge.customer.account.domain.Customer;
import com.evcharge.customer.account.domain.CustomerStatus;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
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
    public UUID register(RegisterCommand command) {
        List<ContactPoint> points = contactPoints(command);
        if (points.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CONTACT_REQUIRED", "Email or phone is required");
        }
        points.forEach(this::requireUnused);
        Customer customer = new Customer(UUID.randomUUID(), command.firstName().trim(), command.lastName().trim(),
                targetOf(points, Channel.EMAIL), targetOf(points, Channel.PHONE),
                passwords.hash(command.password()), CustomerStatus.PENDING_VERIFICATION, 0, null, clock.instant());
        customers.save(customer);
        points.forEach(point -> verification.issue(customer, point));
        return customer.id();
    }

    private List<ContactPoint> contactPoints(RegisterCommand command) {
        List<ContactPoint> points = new ArrayList<>();
        if (hasText(command.email())) {
            points.add(new ContactPoint(Channel.EMAIL, contacts.email(command.email())));
        }
        if (hasText(command.phone())) {
            points.add(new ContactPoint(Channel.PHONE, contacts.phone(command.phone())));
        }
        return points;
    }

    private void requireUnused(ContactPoint point) {
        if (customers.findByContact(point.channel(), point.target()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "CONTACT_ALREADY_USED", "Contact already registered");
        }
    }

    private static String targetOf(List<ContactPoint> points, Channel channel) {
        return points.stream().filter(point -> point.channel() == channel)
                .map(ContactPoint::target).findFirst().orElse(null);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
