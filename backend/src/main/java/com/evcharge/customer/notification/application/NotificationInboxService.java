package com.evcharge.customer.notification.application;

import com.evcharge.customer.notification.application.port.NotificationInbox;
import com.evcharge.customer.notification.domain.Inbox;
import com.evcharge.customer.notification.domain.InboxEntry;
import com.evcharge.customer.notification.domain.InboxItem;
import com.evcharge.customer.notification.domain.PushContent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class NotificationInboxService {

    private static final int MAXIMUM = 100;

    private final NotificationInbox inbox;
    private final NotificationTexts texts;
    private final Clock clock;

    NotificationInboxService(NotificationInbox inbox, NotificationTexts texts, Clock clock) {
        this.inbox = inbox;
        this.texts = texts;
        this.clock = clock;
    }

    public Inbox list(UUID customerId, String language, int limit) {
        var entries = inbox.recent(customerId, Math.clamp(limit, 1, MAXIMUM)).stream()
                .map(item -> entryOf(item, language)).toList();
        return new Inbox(inbox.unread(customerId), entries);
    }

    public int unread(UUID customerId) {
        return inbox.unread(customerId);
    }

    @Transactional
    public void markRead(UUID customerId, UUID id) {
        inbox.markRead(customerId, id, clock.instant());
    }

    @Transactional
    public void markAllRead(UUID customerId) {
        inbox.markAllRead(customerId, clock.instant());
    }

    private InboxEntry entryOf(InboxItem item, String language) {
        PushContent content = texts.render(item.id(), item.type(), item.params(), language);
        return new InboxEntry(item.id(), item.type().name(), content.title(), content.body(),
                item.params().get("screen"), item.params().get("id"), item.createdAt(), item.read());
    }
}
