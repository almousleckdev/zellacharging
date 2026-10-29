package com.evcharge.customer.shared;

import java.time.Instant;

public record ActivityEntry(String action, String detail, String correlationId, Instant createdAt) {
}
