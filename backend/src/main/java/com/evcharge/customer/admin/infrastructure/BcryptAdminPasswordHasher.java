package com.evcharge.customer.admin.infrastructure;

import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class BcryptAdminPasswordHasher implements AdminPasswordHasher {

    private final PasswordEncoder encoder;

    BcryptAdminPasswordHasher(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hash) {
        return encoder.matches(rawPassword, hash);
    }
}
