package com.evcharge.customer.admin.application;

import java.util.List;

public record IncidentListing(long total, long open, long openHigh, int page, int size, List<IncidentView> items) {
}
