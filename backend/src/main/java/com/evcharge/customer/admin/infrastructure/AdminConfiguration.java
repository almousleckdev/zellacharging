package com.evcharge.customer.admin.infrastructure;

import com.evcharge.customer.admin.application.AdminSettings;
import com.evcharge.customer.admin.application.port.SecretCipher;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Configuration
@EnableConfigurationProperties(AdminSettings.class)
class AdminConfiguration {

    private static final int MIN_PASSPHRASE_LENGTH = 32;

    @Bean
    SecretCipher totpSecretCipher(AdminSettings settings) throws NoSuchAlgorithmException {
        if (settings.totpKey() == null || settings.totpKey().length() < MIN_PASSPHRASE_LENGTH) {
            throw new IllegalStateException("zella.admin.totp-key must be at least 32 characters");
        }
        byte[] key = MessageDigest.getInstance("SHA-256").digest(settings.totpKey().getBytes(StandardCharsets.UTF_8));
        return new AesGcmSecretCipher(new SecretKeySpec(key, "AES"));
    }
}
