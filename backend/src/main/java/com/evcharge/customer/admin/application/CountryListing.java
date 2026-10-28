package com.evcharge.customer.admin.application;

import java.util.List;

public record CountryListing<T>(long total, int page, int size, List<CountryEntry<T>> items) {
}
