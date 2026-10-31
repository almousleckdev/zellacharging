package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.CustomerIdentityRepository;
import com.evcharge.customer.account.application.port.SocialTokenVerifier;
import com.evcharge.customer.account.domain.SocialProfile;
import com.evcharge.customer.account.domain.SocialProvider;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class SocialReauthentication {

    private final SocialTokenVerifier verifier;
    private final CustomerIdentityRepository identities;

    SocialReauthentication(SocialTokenVerifier verifier, CustomerIdentityRepository identities) {
        this.verifier = verifier;
        this.identities = identities;
    }

    void confirm(UUID customerId, SocialProvider provider, String idToken) {
        if (provider == null || idToken == null || idToken.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "SOCIAL_PROOF_REQUIRED",
                    "Sign in again with your provider to confirm");
        }
        SocialProfile profile = verifier.verify(provider, idToken);
        if (!identities.isLinked(customerId, provider, profile.subject())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "SOCIAL_IDENTITY_MISMATCH",
                    "This provider account is not linked to your account");
        }
    }
}
