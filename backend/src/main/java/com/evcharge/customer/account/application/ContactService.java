package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.application.port.PendingEmailRepository;
import com.evcharge.customer.account.application.port.PendingPhoneRepository;
import com.evcharge.customer.account.domain.Channel;
import com.evcharge.customer.account.domain.CodeDelivery;
import com.evcharge.customer.account.domain.ContactPoint;
import com.evcharge.customer.account.domain.Customer;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

@Service
public class ContactService {

    private final ProfileService profiles;
    private final CustomerRepository customers;
    private final PendingEmailRepository pending;
    private final PendingPhoneRepository pendingPhones;
    private final VerificationService verification;
    private final ContactNormalizer contacts;
    private final AccountCountries countries;
    private final Clock clock;

    public ContactService(ProfileService profiles, CustomerRepository customers, PendingEmailRepository pending,
                          PendingPhoneRepository pendingPhones, VerificationService verification, ContactNormalizer contacts, AccountCountries countries,
                          Clock clock) {
        this.profiles = profiles;
        this.customers = customers;
        this.pending = pending;
        this.pendingPhones = pendingPhones;
        this.verification = verification;
        this.contacts = contacts;
        this.countries = countries;
        this.clock = clock;
    }

    @Transactional
    public void startEmail(UUID customerId, String rawEmail) {
        Customer customer = profiles.get(customerId);
        ContactPoint email = new ContactPoint(Channel.EMAIL, contacts.email(rawEmail));
        requireUnused(email);
        pending.save(customerId, email.target(), clock.instant());
        verification.reissue(customer, email);
    }

    @Transactional
    public void sendEmailCode(UUID customerId) {
        Customer customer = profiles.get(customerId);
        if (customer.email() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "NO_EMAIL", "No email address to verify");
        }
        if (customers.isVerified(customerId, Channel.EMAIL)) {
            throw conflict("EMAIL_ALREADY_VERIFIED");
        }
        verification.reissue(customer, new ContactPoint(Channel.EMAIL, customer.email()));
    }

    @Transactional(noRollbackFor = ApiException.class)
    public void confirmEmail(UUID customerId, String code) {
        Customer customer = profiles.get(customerId);
        Optional<String> pendingEmail = pending.find(customerId);
        if (pendingEmail.isEmpty() && (customer.email() == null || customers.isVerified(customerId, Channel.EMAIL))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "NO_PENDING_EMAIL",
                    "No email is waiting for confirmation");
        }
        verification.confirm(customer, Channel.EMAIL, code);
        pendingEmail.ifPresent(email -> customers.attachEmail(customerId, email, clock.instant()));
        customers.markContactVerified(customerId, Channel.EMAIL, clock.instant());
        pending.delete(customerId);
    }

    @Transactional
    public void startPhone(UUID customerId, String rawPhone, CodeDelivery delivery) {
        requirePhoneDelivery(delivery);
        Customer customer = profiles.get(customerId);
        ContactPoint phone = new ContactPoint(Channel.PHONE, contacts.phone(rawPhone));
        if (!countries.forTarget(Channel.PHONE, phone.target()).equals(TenantContext.require())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PHONE_COUNTRY_MISMATCH",
                    "The number does not belong to your country");
        }
        requireUnused(phone);
        pendingPhones.save(customerId, phone.target(), clock.instant());
        verification.reissue(customer, phone, Channel.PHONE, delivery);
    }

    @Transactional
    public void sendPhoneCode(UUID customerId, CodeDelivery delivery) {
        requirePhoneDelivery(delivery);
        Customer customer = profiles.get(customerId);
        Optional<String> pendingPhone = pendingPhones.find(customerId);
        String target = pendingPhone.orElse(customer.phone());
        if (target == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "NO_PHONE", "No phone number to verify");
        }
        if (pendingPhone.isEmpty() && customers.isVerified(customerId, Channel.PHONE)) {
            throw conflict("PHONE_ALREADY_VERIFIED");
        }
        verification.reissue(customer, new ContactPoint(Channel.PHONE, target), Channel.PHONE, delivery);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public void confirmPhone(UUID customerId, String code) {
        Customer customer = profiles.get(customerId);
        Optional<String> pendingPhone = pendingPhones.find(customerId);
        if (pendingPhone.isEmpty() && (customer.phone() == null || customers.isVerified(customerId, Channel.PHONE))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "NO_PENDING_PHONE",
                    "No phone number is waiting for confirmation");
        }
        verification.confirm(customer, Channel.PHONE, code);
        pendingPhone.ifPresent(phone -> customers.attachPhone(customerId, phone));
        customers.markContactVerified(customerId, Channel.PHONE, clock.instant());
        pendingPhones.delete(customerId);
    }

    private static void requirePhoneDelivery(CodeDelivery delivery) {
        if (delivery == null || !delivery.isPhone()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CHANNEL_NOT_SUPPORTED",
                    "Choose WhatsApp or SMS to receive the code");
        }
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
