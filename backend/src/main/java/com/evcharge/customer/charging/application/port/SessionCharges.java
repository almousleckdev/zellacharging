package com.evcharge.customer.charging.application.port;

import com.evcharge.customer.charging.domain.SessionCharge;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SessionCharges {

    void insertIfAbsent(SessionCharge charge);

    List<UUID> terminalSessionsWithoutCharge(int limit, Collection<UUID> excluded);
}
