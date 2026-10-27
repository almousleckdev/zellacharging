package com.evcharge.customer.admin.application;

import java.util.UUID;

public interface AdminAudit {
    void record(UUID adminId, String country, String action, String subject, String detail);
}
