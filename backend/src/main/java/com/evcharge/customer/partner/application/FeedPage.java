package com.evcharge.customer.partner.application;

import com.evcharge.customer.charging.ChargeFeedItem;

import java.util.List;

public record FeedPage(List<ChargeFeedItem> items, String nextCursor, boolean hasMore) {
}
