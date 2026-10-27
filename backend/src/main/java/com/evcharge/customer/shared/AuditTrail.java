package com.evcharge.customer.shared;

import java.util.UUID;

public interface AuditTrail {

    void record(String action, UUID actorId, String subject, String detail);
}
