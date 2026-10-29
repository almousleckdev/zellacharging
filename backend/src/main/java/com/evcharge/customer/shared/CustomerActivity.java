package com.evcharge.customer.shared;

import java.util.List;
import java.util.UUID;

public interface CustomerActivity {

    long count(UUID customerId);

    List<ActivityEntry> latest(UUID customerId, int limit);
}
