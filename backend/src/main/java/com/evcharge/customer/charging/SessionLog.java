package com.evcharge.customer.charging;

import java.util.Optional;
import java.util.UUID;

public interface SessionLog {

    SessionPage search(SessionLogQuery query);

    Optional<SessionDetail> find(UUID id);
}
