package com.evcharge.customer.admin.application;

import java.util.List;

public record ActivityListing(long total, int page, int size, List<ActivityView> items) {
}
