package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.application.port.AdminTokenIssuer;
import com.evcharge.customer.admin.application.port.SecretCipher;
import com.evcharge.customer.admin.application.port.TotpAuthenticator;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.admin.domain.AdminStatus;
import com.evcharge.customer.admin.domain.AdminUser;
import com.evcharge.customer.shared.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminLoginTimingTest {

    private static final String DUMMY_HASH = "hash-of-a-password-nobody-knows";

    private final AdminRepository admins = mock(AdminRepository.class);
    private final AdminPasswordHasher passwords = mock(AdminPasswordHasher.class);
    private final AdminFailures failures = mock(AdminFailures.class);
    private AdminLoginService logins;

    @BeforeEach
    void service() {
        when(passwords.hash(anyString())).thenReturn(DUMMY_HASH);
        logins = new AdminLoginService(admins, passwords, mock(AdminTokenIssuer.class), mock(TotpAuthenticator.class),
                mock(SecretCipher.class), failures, mock(AdminSettings.class));
    }

    private void assertInvalidCredentials(String email) {
        assertThatThrownBy(() -> logins.login(email, "Some-Password-1")).isInstanceOf(ApiException.class)
                .extracting(error -> ((ApiException) error).code()).isEqualTo("INVALID_CREDENTIALS");
    }

    @Test
    void anUnknownEmailStillCostsOnePasswordComparison() {
        when(admins.findByEmail("nobody@zella.test")).thenReturn(Optional.empty());

        assertInvalidCredentials("nobody@zella.test");

        verify(passwords).matches("Some-Password-1", DUMMY_HASH);
        verify(failures, never()).register(any());
    }

    @Test
    void aDisabledAdminAlsoCostsOnePasswordComparison() {
        AdminUser disabled = AdminUser.create(UUID.randomUUID(), "off@zella.test", "real-hash", AdminRole.SUPER_ADMIN,
                AdminStatus.DISABLED, Set.of(), Set.of());
        when(admins.findByEmail("off@zella.test")).thenReturn(Optional.of(disabled));

        assertInvalidCredentials("off@zella.test");

        verify(passwords).matches("Some-Password-1", DUMMY_HASH);
        verify(passwords, never()).matches(eq("Some-Password-1"), eq("real-hash"));
    }

    @Test
    void theDummyHashIsComputedOnceNotPerLogin() {
        when(admins.findByEmail(anyString())).thenReturn(Optional.empty());

        assertInvalidCredentials("a@zella.test");
        assertInvalidCredentials("b@zella.test");

        verify(passwords).hash(anyString());
    }
}
