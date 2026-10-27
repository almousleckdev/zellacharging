package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.admin.domain.AdminUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminBootstrapTest {

    private AdminRepository admins;
    private AdminPasswordHasher passwords;

    @BeforeEach
    void wire() {
        admins = mock(AdminRepository.class);
        passwords = mock(AdminPasswordHasher.class);
        when(passwords.hash("Str0ng-Pass!")).thenReturn("hashed");
    }

    private AdminBootstrap bootstrap(String email, String password) {
        AdminSettings settings = new AdminSettings(email, password, "k".repeat(32), "Zella", "no-reply@zella.test",
                "http://localhost:3000", Duration.ofHours(72), Duration.ofMinutes(5), Duration.ofMinutes(10),
                Duration.ofHours(12), 5, Duration.ofMinutes(15));
        return new AdminBootstrap(admins, passwords, settings, Clock.systemUTC());
    }

    @Test
    void createsTheSuperAdminWithAHashedPasswordAndNoTotpYet() {
        bootstrap(" Root@Zella.test ", "Str0ng-Pass!").run(null);
        ArgumentCaptor<AdminUser> created = ArgumentCaptor.forClass(AdminUser.class);
        verify(admins).create(created.capture(), any());
        assertThat(created.getValue().email()).isEqualTo("root@zella.test");
        assertThat(created.getValue().passwordHash()).isEqualTo("hashed");
        assertThat(created.getValue().role()).isEqualTo(AdminRole.SUPER_ADMIN);
        assertThat(created.getValue().hasTotp()).isFalse();
    }

    @Test
    void doesNothingWhenASuperAdminAlreadyExists() {
        when(admins.existsSuperAdmin()).thenReturn(true);
        bootstrap("root@zella.test", "Str0ng-Pass!").run(null);
        verify(admins, never()).create(any(), any());
    }

    @Test
    void doesNothingWithoutBothCredentials() {
        bootstrap("root@zella.test", "").run(null);
        bootstrap(null, "Str0ng-Pass!").run(null);
        verify(admins, never()).create(any(), any());
    }
}
