package com.evcharge.customer.admin.infrastructure;

import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.domain.AdminUser;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcAdminRepository implements AdminRepository {

    private final JdbcClient jdbc;

    JdbcAdminRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<AdminUser> findByEmail(String email) {
        return jdbc.sql("select * from admin_user where lower(email) = lower(:email)").param("email", email)
                .query((rs, rowNum) -> AdminRows.map(rs)).optional().map(admin -> AdminRows.withAccess(jdbc, admin));
    }

    @Override
    public Optional<AdminUser> findById(UUID id) {
        return jdbc.sql("select * from admin_user where id = :id").param("id", id)
                .query((rs, rowNum) -> AdminRows.map(rs)).optional().map(admin -> AdminRows.withAccess(jdbc, admin));
    }

    @Override
    public boolean existsSuperAdmin() {
        return jdbc.sql("select exists (select 1 from admin_user where role = 'SUPER_ADMIN')")
                .query(Boolean.class).single();
    }

    @Override
    public void create(AdminUser admin, Instant at) {
        jdbc.sql("insert into admin_user (id, email, password_hash, role, status, first_name, last_name, "
                        + "phone_e164, totp_secret, created_at) values (:id, :email, :hash, :role, :status, :first, "
                        + ":last, :phone, :totp, :at)")
                .param("id", admin.id()).param("email", admin.email()).param("hash", admin.passwordHash())
                .param("role", admin.role().name()).param("status", admin.status().name())
                .param("first", admin.firstName()).param("last", admin.lastName()).param("phone", admin.phone())
                .param("totp", admin.totpSecret()).param("at", Timestamps.toDb(at)).update();
        for (Permission permission : admin.permissions()) {
            jdbc.sql("insert into admin_permission (admin_id, permission) values (:id, :permission)")
                    .param("id", admin.id()).param("permission", permission.name()).update();
        }
        for (String country : admin.grantedCountries()) {
            jdbc.sql("insert into admin_country_grant (admin_id, country_code) values (:id, :country)")
                    .param("id", admin.id()).param("country", country).update();
        }
    }

    @Override
    public void registerFailure(UUID id, int maxFailures, Instant lockedUntil) {
        jdbc.sql("""
                        update admin_user set
                            failed_attempts = case when failed_attempts + 1 >= :max then 0 else failed_attempts + 1 end,
                            locked_until = case when failed_attempts + 1 >= :max then :locked else locked_until end
                        where id = :id""")
                .param("max", maxFailures).param("locked", Timestamps.toDb(lockedUntil)).param("id", id).update();
    }

    @Override
    public void clearFailures(UUID id) {
        jdbc.sql("update admin_user set failed_attempts = 0, locked_until = null where id = :id")
                .param("id", id).update();
    }

    @Override
    public void updatePassword(UUID id, String passwordHash) {
        jdbc.sql("update admin_user set password_hash = :hash where id = :id")
                .param("hash", passwordHash).param("id", id).update();
    }

    @Override
    public void recordLogin(UUID id, Instant at) {
        jdbc.sql("update admin_user set last_login_at = :at where id = :id")
                .param("at", Timestamps.toDb(at)).param("id", id).update();
    }

    @Override
    public void storePendingSecret(UUID id, String encryptedSecret) {
        jdbc.sql("update admin_user set totp_pending_secret = :secret where id = :id and totp_secret is null")
                .param("secret", encryptedSecret).param("id", id).update();
    }

    @Override
    public boolean activateTotp(UUID id, String encryptedSecret, long step) {
        return jdbc.sql("update admin_user set totp_secret = :secret, totp_pending_secret = null, "
                        + "totp_last_step = :step where id = :id and totp_secret is null "
                        + "and totp_pending_secret = :secret")
                .param("secret", encryptedSecret).param("step", step).param("id", id).update() == 1;
    }

    @Override
    public boolean advanceTotpStep(UUID id, long step) {
        return jdbc.sql("update admin_user set totp_last_step = :step where id = :id "
                        + "and (totp_last_step is null or totp_last_step < :step)")
                .param("step", step).param("id", id).update() == 1;
    }
}
