package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.application.port.VerificationCodeRepository;
import com.evcharge.customer.account.application.port.VerificationSender;
import com.evcharge.customer.account.domain.Channel;
import com.evcharge.customer.account.domain.ContactPoint;
import com.evcharge.customer.account.domain.Customer;
import com.evcharge.customer.account.domain.CustomerStatus;
import com.evcharge.customer.account.domain.Sha256;
import com.evcharge.customer.account.domain.VerificationCode;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class VerificationService {

    private final SecureRandom random = new SecureRandom();
    private final CustomerRepository customers;
    private final VerificationCodeRepository codes;
    private final VerificationSender sender;
    private final ContactNormalizer contacts;
    private final AccountSettings settings;
    private final Clock clock;

    public VerificationService(CustomerRepository customers, VerificationCodeRepository codes,
                               VerificationSender sender, ContactNormalizer contacts,
                               AccountSettings settings, Clock clock) {
        this.customers = customers;
        this.codes = codes;
        this.sender = sender;
        this.contacts = contacts;
        this.settings = settings;
        this.clock = clock;
    }

    @Transactional
    public void issue(Customer customer, ContactPoint contact) {
        String code = "%06d".formatted(random.nextInt(1_000_000));
        Instant now = clock.instant();
        codes.save(new VerificationCode(UUID.randomUUID(), customer.id(), contact.channel(),
                hash(customer.id(), contact.channel(), code),
                now.plus(settings.verification().codeTtl()), 0, null, now));
        sender.send(contact.channel(), contact.target(), code);
    }

    @Transactional
    public void resend(Channel channel, String target) {
        ContactPoint contact = contacts.of(channel, target);
        customers.findByContact(contact.channel(), contact.target())
                .filter(customer -> customer.status() != CustomerStatus.BLOCKED)
                .ifPresent(customer -> reissue(customer, contact));
    }

    @Transactional(noRollbackFor = ApiException.class)
    public void verify(Channel channel, String target, String submitted) {
        ContactPoint contact = contacts.of(channel, target);
        Customer customer = customers.findByContact(contact.channel(), contact.target())
                .orElseThrow(VerificationService::invalidCode);
        VerificationCode code = codes.findLatest(customer.id(), channel)
                .filter(candidate -> candidate.isUsableAt(clock.instant(), settings.verification().maxAttempts()))
                .orElseThrow(VerificationService::invalidCode);
        if (!code.codeHash().equals(hash(customer.id(), channel, submitted))) {
            codes.incrementAttempts(code.id());
            throw invalidCode();
        }
        codes.consume(code.id(), clock.instant());
        customers.markContactVerified(customer.id(), channel, clock.instant());
    }

    private void reissue(Customer customer, ContactPoint contact) {
        boolean tooSoon = codes.findLatest(customer.id(), contact.channel())
                .filter(previous -> previous.createdAt()
                        .plus(settings.verification().resendCooldown()).isAfter(clock.instant()))
                .isPresent();
        if (tooSoon) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "VERIFICATION_COOLDOWN",
                    "Please wait before requesting another code");
        }
        issue(customer, contact);
    }

    private String hash(UUID customerId, Channel channel, String code) {
        return Sha256.hex(settings.security().codePepper() + ":" + customerId + ":" + channel + ":" + code);
    }

    private static ApiException invalidCode() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CODE", "Invalid or expired code");
    }
}
