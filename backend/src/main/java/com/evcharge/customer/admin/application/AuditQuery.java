package com.evcharge.customer.admin.application;

import com.evcharge.customer.shared.LogFilter;

import java.util.List;

public record AuditQuery(List<String> countries, boolean includeGlobal, String action, String text, String subject,
                         LogFilter filter,
                         int limit, int offset) {
}
