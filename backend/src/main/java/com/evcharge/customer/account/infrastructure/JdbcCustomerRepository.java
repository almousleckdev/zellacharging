package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.shared.Timestamps;
import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.domain.Channel;
import com.evcharge.customer.account.domain.Customer;
import com.evcharge.customer.account.domain.CustomerStatus;
import com.evcharge.customer.shared.ApiException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcCustomerRepository implements CustomerRepository {

    private static final RowMapper<Customer> MAPPER = (rs, rowNum) -> new Customer(
            rs.getObject("id", UUID.class),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email"),
            rs.getString("phone_e164"),
            rs.getString("password_hash"),
            CustomerStatus.valueOf(rs.getString("status")),
            rs.getInt("failed_login_attempts"),
            Timestamps.read(rs, "locked_until"),
            Timestamps.read(rs, "created_at"));

    private final JdbcClient jdbc;

    JdbcCustomerRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(Customer customer) {
        try {
            jdbc.sql("""
                    insert into customer (id, first_name, last_name, email, phone_e164, password_hash,
                                          status, failed_login_attempts, locked_until, created_at)
                    values (:id, :firstName, :lastName, :email, :phone, :hash, :status, :attempts, :lockedUntil, :createdAt)
                    """)
                    .param("id", customer.id())
                    .param("firstName", customer.firstName())
                    .param("lastName", customer.lastName())
                    .param("email", customer.email())
                    .param("phone", customer.phone())
                    .param("hash", customer.passwordHash())
                    .param("status", customer.status().name())
                    .param("attempts", customer.failedLoginAttempts())
                    .param("lockedUntil", Timestamps.toDb(customer.lockedUntil()))
                    .param("createdAt", Timestamps.toDb(customer.createdAt()))
                    .update();
        } catch (DuplicateKeyException e) {
            throw new ApiException(HttpStatus.CONFLICT, "CONTACT_ALREADY_USED", "Contact already registered");
        }
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return jdbc.sql("select * from customer where id = :id").param("id", id).query(MAPPER).optional();
    }

    @Override
    public Optional<Customer> findByContact(Channel channel, String target) {
        String column = channel == Channel.EMAIL ? "email" : "phone_e164";
        return jdbc.sql("select * from customer where " + column + " = :target")
                .param("target", target).query(MAPPER).optional();
    }

    @Override
    public void markContactVerified(UUID id, Channel channel, Instant at) {
        String column = channel == Channel.EMAIL ? "email_verified_at" : "phone_verified_at";
        jdbc.sql("update customer set " + column + " = :at, status = case when status = 'PENDING_VERIFICATION' "
                        + "then 'ACTIVE' else status end where id = :id")
                .param("at", Timestamps.toDb(at)).param("id", id).update();
    }

    @Override
    public void recordFailedLogin(UUID id, int attempts, Instant lockedUntil) {
        jdbc.sql("update customer set failed_login_attempts = :attempts, locked_until = :lockedUntil where id = :id")
                .param("attempts", attempts).param("lockedUntil", Timestamps.toDb(lockedUntil))
                .param("id", id).update();
    }

    @Override
    public void clearFailedLogins(UUID id) {
        recordFailedLogin(id, 0, null);
    }

    @Override
    public void updateName(UUID id, String firstName, String lastName) {
        jdbc.sql("update customer set first_name = :first, last_name = :last where id = :id")
                .param("first", firstName).param("last", lastName).param("id", id).update();
    }

    @Override
    public void updatePasswordHash(UUID id, String passwordHash) {
        jdbc.sql("update customer set password_hash = :hash, failed_login_attempts = 0, locked_until = null "
                        + "where id = :id")
                .param("hash", passwordHash).param("id", id).update();
    }

    @Override
    public void markDeleted(UUID id, String unusablePasswordHash) {
        jdbc.sql("""
                update customer set status = 'DELETED', first_name = 'Deleted', last_name = 'Account',
                    email = null, phone_e164 = null, email_verified_at = null, phone_verified_at = null,
                    password_hash = :hash
                where id = :id
                """)
                .param("hash", unusablePasswordHash).param("id", id).update();
    }
}
