package com.evcharge.customer.admin.application;

import com.evcharge.customer.charging.UsageSummary;
import com.evcharge.customer.shared.Country;

public record CountryUsage(Country country, UsageSummary summary) {
}
