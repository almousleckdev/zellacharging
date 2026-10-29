package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AuditEntry;
import com.evcharge.customer.admin.application.AuditPage;

import java.util.List;

record AuditListResponse(long total, int page, int size, List<AuditEntry> items, List<String> actions) {

    static AuditListResponse from(AuditPage found, int page, int size, List<String> actions) {
        return new AuditListResponse(found.total(), page, size, found.items(), actions);
    }
}
