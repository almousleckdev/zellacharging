package com.evcharge.customer.notification.domain;

import java.util.UUID;

public record DeviceToken(UUID customerId, String token, Platform platform, String language) {
}
