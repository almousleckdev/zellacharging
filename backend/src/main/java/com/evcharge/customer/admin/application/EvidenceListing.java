package com.evcharge.customer.admin.application;

import java.util.List;

public record EvidenceListing(long total, int page, int size, List<AdminEvidence> items) {
}
