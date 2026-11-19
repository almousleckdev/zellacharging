package com.evcharge.customer.charging;

import java.util.UUID;

public interface UsageFigures {

    UsageSummary summary(String station, UUID customerId);
}
