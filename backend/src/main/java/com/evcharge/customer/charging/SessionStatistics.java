package com.evcharge.customer.charging;

import java.util.UUID;

public interface SessionStatistics {

    SessionFigures figures();

    CustomerSessions ofCustomer(UUID customerId);
}
