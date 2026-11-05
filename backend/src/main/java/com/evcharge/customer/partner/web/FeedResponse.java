package com.evcharge.customer.partner.web;

import com.evcharge.customer.partner.application.FeedPage;

import java.util.List;

record FeedResponse(List<FeedItemView> items, String nextCursor, boolean hasMore) {

    static FeedResponse of(FeedPage page) {
        return new FeedResponse(page.items().stream().map(FeedItemView::of).toList(), page.nextCursor(),
                page.hasMore());
    }
}
