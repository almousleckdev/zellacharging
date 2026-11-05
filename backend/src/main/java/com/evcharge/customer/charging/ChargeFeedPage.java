package com.evcharge.customer.charging;

import java.util.List;

public record ChargeFeedPage(List<ChargeFeedItem> items, boolean hasMore) {
}
