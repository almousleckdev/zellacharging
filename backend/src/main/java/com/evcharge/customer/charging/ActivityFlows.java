package com.evcharge.customer.charging;

import com.evcharge.customer.shared.Bucket;

import java.time.Instant;
import java.util.List;

public interface ActivityFlows {

    List<DailyActivity> between(Instant from, Instant to, Bucket bucket);
}
