package com.evcharge.customer.admin.application;

import java.time.Instant;

public record CountryEntry<T>(String country, T item, Instant createdAt) {
}
