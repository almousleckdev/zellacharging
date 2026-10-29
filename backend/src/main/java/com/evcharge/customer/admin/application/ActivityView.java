package com.evcharge.customer.admin.application;

import java.time.Instant;

public record ActivityView(String source, String actor, String action, String detail, Instant createdAt) {
}
