package com.evcharge.customer.notification.application.port;

import com.evcharge.customer.notification.domain.DeviceToken;

import java.util.List;
import java.util.UUID;

public interface DeviceTokenRepository {

    void save(DeviceToken device);

    List<DeviceToken> forCustomer(UUID customerId);

    void remove(UUID customerId, String token);

    void removeToken(String token);
}
