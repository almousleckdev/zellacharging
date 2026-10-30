package com.evcharge.customer.notification.domain;

import java.util.List;

public record Inbox(int unread, List<InboxEntry> entries) {
}
