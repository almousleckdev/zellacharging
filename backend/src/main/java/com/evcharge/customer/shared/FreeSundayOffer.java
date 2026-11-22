package com.evcharge.customer.shared;

import java.time.Instant;

public record FreeSundayOffer(boolean available, String reason, Instant endsAt, FreeSundayWindow window) {

    public FreeSundayOffer(boolean available, String reason, Instant endsAt) {
        this(available, reason, endsAt, null);
    }
}
