package com.evcharge.customer.account.web;

import com.evcharge.customer.account.application.ClosureProof;
import com.evcharge.customer.account.domain.SocialProvider;

record DeleteAccountRequest(String password, SocialProvider provider, String idToken, String authorizationCode) {

    ClosureProof proof() {
        return new ClosureProof(password, provider, idToken, authorizationCode);
    }
}
