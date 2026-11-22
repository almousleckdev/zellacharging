package com.evcharge.customer.shared;

import java.util.UUID;

public interface FreeSundayAccess {

    FreeSundayOffer offer(UUID customerId);

    boolean claimFor(UUID customerId, UUID sessionId);

    boolean holdsClaim(UUID sessionId);

    void release(UUID sessionId);
}
