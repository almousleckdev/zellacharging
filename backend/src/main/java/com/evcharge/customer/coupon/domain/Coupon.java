package com.evcharge.customer.coupon.domain;

import java.time.Instant;
import java.util.UUID;

public record Coupon(UUID id, String title, String description, Instant expiresAt) {
}
