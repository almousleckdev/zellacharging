package com.evcharge.customer.admin.application;

import com.evcharge.customer.charging.FreeUsageRow;

public record CountryFreeUsage(String country, FreeUsageRow usage) {
}
