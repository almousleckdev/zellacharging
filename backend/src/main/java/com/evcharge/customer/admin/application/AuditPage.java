package com.evcharge.customer.admin.application;

import java.util.List;

public record AuditPage(long total, List<AuditEntry> items) {
}
