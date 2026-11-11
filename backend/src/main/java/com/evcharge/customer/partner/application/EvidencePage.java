package com.evcharge.customer.partner.application;

import com.evcharge.customer.partner.domain.EvidenceItem;

import java.util.List;

public record EvidencePage(List<EvidenceItem> items, String nextCursor, boolean hasMore) {
}
