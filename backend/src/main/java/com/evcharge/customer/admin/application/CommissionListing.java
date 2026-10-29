package com.evcharge.customer.admin.application;

import com.evcharge.customer.charging.CommissionRecord;

import java.util.List;

public record CommissionListing(List<CountryCommissionTotals> summaries, CountryListing<CommissionRecord> items) {
}
