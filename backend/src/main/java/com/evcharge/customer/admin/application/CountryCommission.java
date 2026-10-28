package com.evcharge.customer.admin.application;

import com.evcharge.customer.charging.CommissionRecord;
import com.evcharge.customer.charging.CommissionTotals;
import com.evcharge.customer.shared.Country;

import java.util.List;

public record CountryCommission(Country country, CommissionTotals totals, List<CommissionRecord> items) {
}
