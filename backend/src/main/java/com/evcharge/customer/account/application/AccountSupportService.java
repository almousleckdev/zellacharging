package com.evcharge.customer.account.application;

import com.evcharge.customer.account.CustomerSupport;
import com.evcharge.customer.account.CustomerSupportState;
import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.application.port.PendingEmailRepository;
import com.evcharge.customer.account.application.port.PendingPhoneRepository;
import com.evcharge.customer.account.application.port.VerificationCodeRepository;
import com.evcharge.customer.account.domain.Channel;
import com.evcharge.customer.account.domain.CodeDelivery;
import com.evcharge.customer.account.domain.ContactPoint;
import com.evcharge.customer.account.domain.Customer;
import com.evcharge.customer.account.domain.CustomerStatus;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
class AccountSupportService implements CustomerSupport {

    private final CustomerRepository customers;
    private final PendingEmailRepository pendingEmails;
    private final PendingPhoneRepository pendingPhones;
    private final VerificationCodeRepository codes;
    private final VerificationService verification;
    private final ContactChange change;
    private final ProfileService profiles;
    private final Clock clock;

    AccountSupportService(CustomerRepository customers, PendingEmailRepository pendingEmails,
                          PendingPhoneRepository pendingPhones, VerificationCodeRepository codes,
                          VerificationService verification, ContactChange change, ProfileService profiles,
                          Clock clock) {
        this.customers = customers;
        this.pendingEmails = pendingEmails;
        this.pendingPhones = pendingPhones;
        this.codes = codes;
        this.verification = verification;
        this.change = change;
        this.profiles = profiles;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerSupportState state(UUID customerId) {
        Customer customer = load(customerId);
        boolean locked = customer.isLockedAt(clock.instant());
        return new CustomerSupportState(locked ? customer.lockedUntil() : null, customer.failedLoginAttempts(),
                pendingEmails.find(customerId).orElse(null), pendingPhones.find(customerId).orElse(null));
    }

    @Override
    @Transactional
    public void resendCode(UUID customerId, String channelName, String deliveryName) {
        Channel channel = contactChannel(channelName);
        Customer customer = load(customerId);
        String target = targetOf(customer, channel);
        CodeDelivery delivery = deliveryFor(channel, deliveryName);
        verification.issue(customer, new ContactPoint(channel, target), channel, delivery);
    }

    @Override
    @Transactional
    public void resetCodeAttempts(UUID customerId, String channelName) {
        load(customerId);
        codes.resetAttempts(customerId, contactChannel(channelName));
    }

    @Override
    @Transactional
    public void unlockLogin(UUID customerId) {
        load(customerId);
        customers.clearFailedLogins(customerId);
    }

    @Override
    @Transactional
    public void markVerified(UUID customerId, String channelName) {
        Channel channel = contactChannel(channelName);
        Customer customer = load(customerId);
        String pending = (channel == Channel.EMAIL ? pendingEmails.find(customerId)
                : pendingPhones.find(customerId)).orElse(null);
        if (pending != null) {
            change.apply(customerId, channel, pending);
        } else {
            targetOf(customer, channel);
        }
        customers.markContactVerified(customerId, channel, clock.instant());
    }

    @Override
    @Transactional
    public void changeContact(UUID customerId, String channelName, String value) {
        Channel channel = contactChannel(channelName);
        load(customerId);
        change.apply(customerId, channel, value);
    }

    @Override
    @Transactional
    public void changeName(UUID customerId, String firstName, String lastName) {
        load(customerId);
        profiles.updateName(customerId, firstName, lastName);
    }

    private String targetOf(Customer customer, Channel channel) {
        String pending = (channel == Channel.EMAIL ? pendingEmails.find(customer.id())
                : pendingPhones.find(customer.id())).orElse(null);
        String target = pending != null ? pending : channel == Channel.EMAIL ? customer.email() : customer.phone();
        if (target == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, channel == Channel.EMAIL ? "NO_EMAIL" : "NO_PHONE",
                    "The customer has no such contact");
        }
        return target;
    }

    private static CodeDelivery deliveryFor(Channel channel, String name) {
        if (name == null || name.isBlank()) {
            return CodeDelivery.defaultFor(channel);
        }
        CodeDelivery delivery = parse(CodeDelivery.class, name);
        if ((channel == Channel.EMAIL) == delivery.isPhone()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CHANNEL_NOT_SUPPORTED", "Delivery does not fit the contact");
        }
        return delivery;
    }

    private static Channel contactChannel(String name) {
        Channel channel = parse(Channel.class, name);
        if (channel == Channel.PASSWORD_RESET) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CHANNEL_NOT_SUPPORTED",
                    "Choose email or phone");
        }
        return channel;
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String name) {
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CHANNEL_NOT_SUPPORTED", "Unknown value");
        }
    }

    private Customer load(UUID customerId) {
        return customers.findById(customerId).filter(customer -> customer.status() != CustomerStatus.DELETED)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND",
                        "Customer not found"));
    }
}
