package com.evcharge.customer.admin.infrastructure;

import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.admin.domain.AdminStatus;
import com.evcharge.customer.admin.domain.AdminUser;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

final class AdminRows {

    private AdminRows() {
    }

    static AdminUser map(ResultSet rs) throws SQLException {
        return new AdminUser(rs.getObject("id", UUID.class), rs.getString("email"), rs.getString("password_hash"),
                AdminRole.valueOf(rs.getString("role")), AdminStatus.valueOf(rs.getString("status")),
                rs.getString("first_name"), rs.getString("last_name"), rs.getString("phone_e164"),
                rs.getString("totp_secret"), rs.getString("totp_pending_secret"),
                rs.getObject("totp_last_step", Long.class), rs.getInt("failed_attempts"),
                Timestamps.read(rs, "locked_until"), Timestamps.read(rs, "last_login_at"), Set.of(), Set.of());
    }

    static AdminUser withAccess(JdbcClient jdbc, AdminUser admin) {
        Set<Permission> permissions = EnumSet.noneOf(Permission.class);
        for (String name : jdbc.sql("select permission from admin_permission where admin_id = :id")
                .param("id", admin.id()).query(String.class).list()) {
            try {
                permissions.add(Permission.valueOf(name));
            } catch (IllegalArgumentException ignored) {
                continue;
            }
        }
        Set<String> countries = new HashSet<>(jdbc.sql("select country_code from admin_country_grant "
                + "where admin_id = :id").param("id", admin.id()).query(String.class).list());
        return new AdminUser(admin.id(), admin.email(), admin.passwordHash(), admin.role(), admin.status(),
                admin.firstName(), admin.lastName(), admin.phone(), admin.totpSecret(), admin.totpPendingSecret(),
                admin.totpLastStep(), admin.failedAttempts(), admin.lockedUntil(), admin.lastLoginAt(), permissions,
                countries);
    }
}
