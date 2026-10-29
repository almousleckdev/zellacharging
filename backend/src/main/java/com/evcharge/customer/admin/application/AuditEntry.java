package com.evcharge.customer.admin.application;

import java.time.Instant;
import java.util.UUID;

public record AuditEntry(UUID id, UUID adminId, String adminEmail, String country, String action, String subject,
                         String detail, String correlationId, Instant createdAt) {
}
