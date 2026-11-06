package com.evcharge.customer.partner.web;

import com.evcharge.customer.partner.application.FeedPage;

import java.util.List;

record FeedResponse<Item>(List<Item> items, String nextCursor, boolean hasMore) {

    static FeedResponse<FeedItemView> of(FeedPage page) {
        return new FeedResponse<>(page.items().stream().map(FeedItemView::of).toList(), page.nextCursor(),
                page.hasMore());
    }

    static FeedResponse<PublicFeedItemView> ofPublic(FeedPage page) {
        return new FeedResponse<>(page.items().stream().map(PublicFeedItemView::of).toList(), page.nextCursor(),
                page.hasMore());
    }
}
