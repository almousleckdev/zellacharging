package com.evcharge.customer.shared;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FreeSundayWindows {

    List<UUID> sessionsPastWindow(Instant since, Instant now);

    List<UUID> sessionsEndingBetween(Instant from, Instant to);

    Optional<Instant> windowEnd(UUID sessionId);

    List<UUID> releaseOrphans(Instant claimedSince, Instant claimedBefore);
}
