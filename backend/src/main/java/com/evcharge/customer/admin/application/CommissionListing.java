package com.evcharge.customer.admin.application;

import com.evcharge.customer.charging.CommissionDigest;

import java.util.List;

public record CommissionListing(List<CountryCommissionTotals> summaries, CountryListing<CommissionDigest> items) {
}
