package com.evcharge.customer.notification.web;

import com.evcharge.customer.notification.domain.Inbox;
import com.evcharge.customer.notification.domain.InboxEntry;

import java.time.Instant;
import java.util.List;

record InboxResponse(int unread, List<Item> items) {

    record Item(String id, String type, String title, String body, String screen, String entityId,
                Instant createdAt, boolean read) {

        static Item from(InboxEntry entry) {
            return new Item(entry.id().toString(), entry.type(), entry.title(), entry.body(), entry.screen(),
                    entry.entityId(), entry.createdAt(), entry.read());
        }
    }

    static InboxResponse from(Inbox inbox) {
        return new InboxResponse(inbox.unread(), inbox.entries().stream().map(Item::from).toList());
    }
}
