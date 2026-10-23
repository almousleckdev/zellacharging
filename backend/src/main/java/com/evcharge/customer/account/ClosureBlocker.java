package com.evcharge.customer.account;

import java.util.Optional;
import java.util.UUID;

public interface ClosureBlocker {

    Optional<String> blockingCode(UUID customerId);
}
