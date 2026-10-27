package com.evcharge.customer;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

public final class TestTenants {

    private TestTenants() {
    }

    public static RequestPostProcessor customer(UUID id) {
        return customer(id, PostgresTestBase.TENANT);
    }

    public static RequestPostProcessor customer(UUID id, String country) {
        return jwt().jwt(token -> token.subject(id.toString()).claim("cty", country));
    }

    public static RequestPostProcessor anyCustomer() {
        return customer(UUID.randomUUID());
    }
}
