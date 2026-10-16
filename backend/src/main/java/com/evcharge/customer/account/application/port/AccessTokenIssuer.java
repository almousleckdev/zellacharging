package com.evcharge.customer.account.application.port;

import java.time.Duration;
import java.util.UUID;

public interface AccessTokenIssuer {

    String issue(UUID customerId);

    Duration timeToLive();
}
