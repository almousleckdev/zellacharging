package com.evcharge.customer.charging;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

public interface ChargeFeed {

    ChargeFeedPage after(Instant updatedAt, UUID sessionId, int limit, Collection<UUID> customerIds,
                         Duration lag);

    ChargeFeedPage afterExcluding(Instant updatedAt, UUID sessionId, int limit, Collection<UUID> excludedCustomerIds,
                                  Duration lag);
}
