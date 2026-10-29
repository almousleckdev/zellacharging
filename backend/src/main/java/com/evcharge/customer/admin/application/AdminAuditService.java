package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.AdminAuditLog;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.shared.LogFilter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminAuditService {

    private final AdminAccessService access;
    private final AdminAuditLog log;

    public AdminAuditService(AdminAccessService access, AdminAuditLog log) {
        this.access = access;
        this.log = log;
    }

    public AuditPage list(AdminAccess admin, String country, String action, String text, LogFilter filter, int page,
                          int size) {
        List<String> scope = access.scope(admin, Permission.AUDIT_READ, country);
        PageWindow window = PageWindow.of(page, size);
        boolean everywhere = admin.role() == AdminRole.SUPER_ADMIN
                && (country == null || country.isBlank() || AdminAccessService.ALL_COUNTRIES.equalsIgnoreCase(country));
        return log.search(new AuditQuery(scope, everywhere, blank(action), blank(text), null, filter, window.size(),
                window.offset()));
    }

    public List<String> actions(AdminAccess admin) {
        access.require(admin, Permission.AUDIT_READ);
        return log.actions();
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
