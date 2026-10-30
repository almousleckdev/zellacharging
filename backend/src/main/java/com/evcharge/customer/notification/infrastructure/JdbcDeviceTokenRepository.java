package com.evcharge.customer.notification.infrastructure;

import com.evcharge.customer.notification.application.port.DeviceTokenRepository;
import com.evcharge.customer.notification.domain.DeviceToken;
import com.evcharge.customer.notification.domain.Platform;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Repository
class JdbcDeviceTokenRepository implements DeviceTokenRepository {

    private final JdbcClient jdbc;
    private final Clock clock;

    JdbcDeviceTokenRepository(JdbcClient jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Override
    public void save(DeviceToken device) {
        Object now = Timestamps.toDb(clock.instant());
        jdbc.sql("insert into device_token (token, customer_id, platform, language, created_at, last_seen_at) "
                        + "values (:token, :customer, :platform, :language, :now, :now)")
                .param("token", device.token()).param("customer", device.customerId())
                .param("platform", device.platform().name()).param("language", device.language())
                .param("now", now).update();
    }

    @Override
    public List<DeviceToken> forCustomer(UUID customerId) {
        return jdbc.sql("select token, customer_id, platform, language from device_token "
                        + "where customer_id = :customer and country_code = current_setting('app.country', true)")
                .param("customer", customerId)
                .query((rs, row) -> new DeviceToken(rs.getObject("customer_id", UUID.class), rs.getString("token"),
                        Platform.valueOf(rs.getString("platform")), rs.getString("language")))
                .list();
    }

    @Override
    public void remove(UUID customerId, String token) {
        jdbc.sql("delete from device_token where token = :token and customer_id = :customer")
                .param("token", token).param("customer", customerId).update();
    }

    @Override
    public void removeToken(String token) {
        jdbc.sql("delete from device_token where token = :token").param("token", token).update();
    }
}
