package com.evcharge.customer.notification.application;

import com.evcharge.customer.notification.application.port.DeviceTokenRepository;
import com.evcharge.customer.notification.domain.DeviceToken;
import com.evcharge.customer.notification.domain.Platform;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeviceService {

    private final DeviceTokenRepository devices;

    public DeviceService(DeviceTokenRepository devices) {
        this.devices = devices;
    }

    public void register(UUID customerId, String token, Platform platform, String language) {
        devices.removeToken(token);
        devices.save(new DeviceToken(customerId, token, platform, language));
    }

    public void unregister(UUID customerId, String token) {
        devices.remove(customerId, token);
    }
}
