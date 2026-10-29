package com.evcharge.customer.charging;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface SessionStatistics {

    SessionFigures figures();

    CustomerSessions ofCustomer(UUID customerId);

    Map<UUID, CustomerSessions> ofCustomers(Collection<UUID> customerIds);
}
