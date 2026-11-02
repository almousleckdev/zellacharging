package com.evcharge.customer.admin.infrastructure;

import com.evcharge.customer.admin.application.port.AdminAccountRepository;
import com.evcharge.customer.admin.domain.AdminPage;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.admin.domain.AdminStatus;
import com.evcharge.customer.admin.domain.AdminUser;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
class JdbcAdminAccountRepository implements AdminAccountRepository {

    private static final String TEXT_FILTER = " and (lower(coalesce(first_name, '') || ' ' || coalesce(last_name, '')) "
            + "like :pattern escape '\\' or lower(email) like :pattern escape '\\')";

    private final JdbcClient jdbc;

    JdbcAdminAccountRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public AdminPage search(String text, AdminStatus status, int limit, int offset) {
        String where = "true" + (status == null ? "" : " and status = :status") + (text == null ? "" : TEXT_FILTER);
        long total = bind(jdbc.sql("select count(*) from admin_user where " + where), text, status)
                .query(Long.class).single();
        var items = bind(jdbc.sql("select * from admin_user where " + where
                + " order by created_at desc, id limit :limit offset :offset"), text, status)
                .param("limit", limit).param("offset", offset).query((rs, rowNum) -> AdminRows.map(rs)).list()
                .stream().map(admin -> AdminRows.withAccess(jdbc, admin)).toList();
        return new AdminPage(total, items);
    }

    @Override
    public void updateAccess(UUID id, AdminRole role, Set<Permission> permissions, Set<String> countries) {
        jdbc.sql("update admin_user set role = :role where id = :id").param("role", role.name()).param("id", id)
                .update();
        jdbc.sql("delete from admin_permission where admin_id = :id").param("id", id).update();
        jdbc.sql("delete from admin_country_grant where admin_id = :id").param("id", id).update();
        for (Permission permission : permissions) {
            jdbc.sql("insert into admin_permission (admin_id, permission) values (:id, :permission)")
                    .param("id", id).param("permission", permission.name()).update();
        }
        for (String country : countries) {
            jdbc.sql("insert into admin_country_grant (admin_id, country_code) values (:id, :country)")
                    .param("id", id).param("country", country).update();
        }
    }

    @Override
    public void updateStatus(UUID id, AdminStatus status) {
        jdbc.sql("update admin_user set status = :status where id = :id").param("status", status.name())
                .param("id", id).update();
    }

    @Override
    public void updateProfile(UUID id, String firstName, String lastName, String phone) {
        jdbc.sql("update admin_user set first_name = :first, last_name = :last, phone_e164 = :phone where id = :id")
                .param("first", firstName).param("last", lastName).param("phone", phone).param("id", id).update();
    }

    @Override
    public void storeInvitation(UUID id, String tokenHash, Instant expiresAt) {
        jdbc.sql("update admin_user set invitation_hash = :hash, invitation_expires_at = :expires where id = :id")
                .param("hash", tokenHash).param("expires", Timestamps.toDb(expiresAt)).param("id", id).update();
    }

    @Override
    public Optional<AdminUser> findByInvitation(String tokenHash, Instant now) {
        return jdbc.sql("select * from admin_user where invitation_hash = :hash and invitation_expires_at > :now "
                        + "and status = 'INVITED'")
                .param("hash", tokenHash).param("now", Timestamps.toDb(now))
                .query((rs, rowNum) -> AdminRows.map(rs)).optional().map(admin -> AdminRows.withAccess(jdbc, admin));
    }

    @Override
    public void completeInvitation(UUID id, String passwordHash) {
        jdbc.sql("update admin_user set password_hash = :hash, status = 'ACTIVE', invitation_hash = null, "
                        + "invitation_expires_at = null where id = :id")
                .param("hash", passwordHash).param("id", id).update();
    }

    @Override
    public void storePasswordReset(UUID id, String tokenHash, Instant expiresAt) {
        jdbc.sql("update admin_user set password_reset_hash = :hash, password_reset_expires_at = :expires "
                        + "where id = :id")
                .param("hash", tokenHash).param("expires", Timestamps.toDb(expiresAt)).param("id", id).update();
    }

    @Override
    public Optional<AdminUser> findByPasswordReset(String tokenHash, Instant now) {
        return jdbc.sql("select * from admin_user where password_reset_hash = :hash "
                        + "and password_reset_expires_at > :now and status = 'ACTIVE'")
                .param("hash", tokenHash).param("now", Timestamps.toDb(now))
                .query((rs, rowNum) -> AdminRows.map(rs)).optional().map(admin -> AdminRows.withAccess(jdbc, admin));
    }

    @Override
    public void completePasswordReset(UUID id, String passwordHash) {
        jdbc.sql("update admin_user set password_hash = :hash, password_reset_hash = null, "
                        + "password_reset_expires_at = null, failed_attempts = 0, locked_until = null where id = :id")
                .param("hash", passwordHash).param("id", id).update();
    }

    @Override
    public boolean deleteInvited(UUID id) {
        return jdbc.sql("delete from admin_user where id = :id and status = 'INVITED'").param("id", id)
                .update() == 1;
    }

    private static JdbcClient.StatementSpec bind(JdbcClient.StatementSpec spec, String text, AdminStatus status) {
        if (status != null) {
            spec = spec.param("status", status.name());
        }
        if (text != null) {
            spec = spec.param("pattern", "%" + text.toLowerCase().replace("\\", "\\\\").replace("%", "\\%")
                    .replace("_", "\\_") + "%");
        }
        return spec;
    }
}
