package com.evcharge.customer.admin.application;

import java.util.List;

public record CustomerListing(long total, int page, int size, List<CountryCustomer> items) {
}
