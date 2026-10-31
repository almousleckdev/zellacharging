package com.evcharge.customer.account.application;

import com.evcharge.customer.account.domain.SocialProvider;

public record ClosureProof(String password, SocialProvider provider, String idToken, String authorizationCode) {
}
