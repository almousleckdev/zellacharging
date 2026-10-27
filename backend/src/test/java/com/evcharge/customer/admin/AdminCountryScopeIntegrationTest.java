package com.evcharge.customer.admin;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.application.AdminAccessService;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.admin.domain.AdminStatus;
import com.evcharge.customer.admin.domain.AdminUser;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.shared.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AdminCountryScopeIntegrationTest extends PostgresTestBase {

    @Autowired
    AdminRepository admins;

    @Autowired
    AdminAccessService access;

    private AdminAccess create(AdminRole role, Set<String> grants) {
        UUID id = UUID.randomUUID();
        admins.create(AdminUser.create(id, id + "@zella.test", "hash", role, AdminStatus.ACTIVE, role.permissions(),
                grants).withName("Test", "Admin"), Instant.now());
        return access.load(id);
    }

    @Test
    void aCountryAdminOnlySeesAndQueriesItsOwnCountry() {
        AdminAccess gabon = create(AdminRole.ADMIN, Set.of("GA"));
        assertThat(gabon.countries()).containsExactly("GA");
        assertThat(access.scope(gabon, Permission.OVERVIEW_READ, "ALL")).containsExactly("GA");
        assertThat(access.scope(gabon, Permission.OVERVIEW_READ, "ga")).containsExactly("GA");
        assertThatThrownBy(() -> access.scope(gabon, Permission.OVERVIEW_READ, "ML"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo("COUNTRY_FORBIDDEN"));
    }

    @Test
    void aSuperAdminSpansEveryCountry() {
        AdminAccess root = create(AdminRole.SUPER_ADMIN, Set.of());
        assertThat(access.scope(root, Permission.OVERVIEW_READ, "ALL")).isEqualTo(List.of("GA", "ML"));
        assertThat(access.scope(root, Permission.OVERVIEW_READ, "ML")).containsExactly("ML");
    }

    @Test
    void permissionsComeFromTheRoleNotTheRequest() {
        AdminAccess gabon = create(AdminRole.ADMIN, Set.of("GA"));
        assertThatThrownBy(() -> access.scope(gabon, Permission.ADMINS_MANAGE, "GA"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo("PERMISSION_DENIED"));
    }
}
