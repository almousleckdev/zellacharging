package com.evcharge.customer.wallet;

import com.evcharge.customer.shared.Bucket;

import java.time.Instant;
import java.util.List;

public interface MoneyFlows {

    List<DailyMoney> between(Instant from, Instant to, Bucket bucket);
}
