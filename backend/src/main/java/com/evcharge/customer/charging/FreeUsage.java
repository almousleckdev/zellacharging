package com.evcharge.customer.charging;

import java.time.Instant;
import java.util.List;

public interface FreeUsage {

    List<FreeUsageRow> byCustomer(String category, Instant from, Instant to);
}
