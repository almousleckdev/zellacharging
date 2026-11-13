package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.CustomerIdentityRepository;
import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.application.port.PasswordHasher;
import com.evcharge.customer.account.application.port.PendingEmailRepository;
import com.evcharge.customer.account.application.port.SocialTokenVerifier;
import com.evcharge.customer.account.domain.Channel;
import com.evcharge.customer.account.domain.Customer;
import com.evcharge.customer.account.domain.CustomerStatus;
import com.evcharge.customer.account.domain.SocialProfile;
import com.evcharge.customer.account.domain.SocialProvider;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.AuditTrail;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class SocialLoginService {

    private final SocialTokenVerifier verifier;
    private final CustomerIdentityRepository identities;
    private final CustomerRepository customers;
    private final PasswordHasher passwords;
    private final ContactNormalizer contacts;
    private final PendingEmailRepository pendingEmails;
    private final SessionIssuer sessions;
    private final SocialAvatarImporter avatars;
    private final AuditTrail audit;
    private final Clock clock;

    public SocialLoginService(SocialTokenVerifier verifier, CustomerIdentityRepository identities,
                              CustomerRepository customers, PasswordHasher passwords, ContactNormalizer contacts,
                              PendingEmailRepository pendingEmails, SessionIssuer sessions, SocialAvatarImporter avatars, AuditTrail audit,
                              Clock clock) {
        this.verifier = verifier;
        this.identities = identities;
        this.customers = customers;
        this.passwords = passwords;
        this.contacts = contacts;
        this.pendingEmails = pendingEmails;
        this.sessions = sessions;
        this.avatars = avatars;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional(noRollbackFor = ApiException.class)
    public AuthTokens signIn(SocialProvider provider, String idToken, String firstName, String lastName) {
        SocialProfile profile = verifier.verify(provider, idToken);
        UUID customerId = identities.customerOf(provider, profile.subject())
                .orElseGet(() -> enroll(profile, firstName, lastName));
        Customer customer = customers.findById(customerId).orElseThrow(SocialLoginService::unknownAccount);
        if (customer.status() == CustomerStatus.BLOCKED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_SUSPENDED", "Account is suspended");
        }
        if (!customer.canAuthenticate()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_NOT_VERIFIED", "Account is not verified");
        }
        avatars.importIfMissing(customerId, profile.pictureUrl());
        audit.record("SOCIAL_LOGIN_SUCCEEDED", customerId, "customer", provider.name());
        return sessions.start(customerId, customer.country());
    }

    private UUID enroll(SocialProfile profile, String firstName, String lastName) {
        if (!profile.emailVerified() || profile.email() == null || profile.email().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "SOCIAL_EMAIL_UNVERIFIED",
                    "The provider did not return a verified email");
        }
        String email = contacts.email(profile.email());
        UUID customerId = customers.findByContact(Channel.EMAIL, email).map(this::adopt)
                .or(() -> awaitingThisEmail(email))
                .orElseGet(() -> create(profile, email, firstName, lastName));
        identities.link(customerId, profile.provider(), profile.subject(), clock.instant());
        return customerId;
    }

    private java.util.Optional<UUID> awaitingThisEmail(String email) {
        return pendingEmails.ownerOf(email).flatMap(customers::findById).map(owner -> {
            customers.attachEmail(owner.id(), email, clock.instant());
            pendingEmails.delete(owner.id());
            return adopt(owner);
        });
    }

    private UUID adopt(Customer existing) {
        if (!customers.isVerified(existing.id(), Channel.EMAIL)) {
            customers.markContactVerified(existing.id(), Channel.EMAIL, clock.instant());
            customers.markPasswordless(existing.id(), passwords.hash(UUID.randomUUID().toString()));
        }
        return existing.id();
    }

    private UUID create(SocialProfile profile, String email, String firstName, String lastName) {
        UUID id = UUID.randomUUID();
        customers.save(new Customer(id, TenantContext.require(),
                pick(firstName, profile.firstName(), email.substring(0, email.indexOf('@'))),
                pick(lastName, profile.lastName(), ""), email, null,
                passwords.hash(UUID.randomUUID().toString()), CustomerStatus.PENDING_VERIFICATION, 0, null,
                clock.instant()));
        customers.markContactVerified(id, Channel.EMAIL, clock.instant());
        customers.markPasswordless(id, passwords.hash(UUID.randomUUID().toString()));
        return id;
    }

    private static String pick(String requested, String fromProvider, String fallback) {
        for (String candidate : new String[]{requested, fromProvider}) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate.trim();
            }
        }
        return fallback;
    }

    private static ApiException unknownAccount() {
        return new ApiException(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND", "Customer not found");
    }
}
