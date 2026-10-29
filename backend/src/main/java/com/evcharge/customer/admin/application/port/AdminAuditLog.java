package com.evcharge.customer.admin.application.port;

import com.evcharge.customer.admin.application.AuditPage;
import com.evcharge.customer.admin.application.AuditQuery;

import java.util.List;

public interface AdminAuditLog {

    AuditPage search(AuditQuery query);

    List<String> actions();
}
