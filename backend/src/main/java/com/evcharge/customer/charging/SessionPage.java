package com.evcharge.customer.charging;

import java.util.List;

public record SessionPage(long total, List<SessionRecord> items) {
}
