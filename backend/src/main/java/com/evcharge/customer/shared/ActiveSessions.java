package com.evcharge.customer.shared;

import java.util.UUID;

public interface ActiveSessions {

    boolean isActive(UUID sessionId);
}
