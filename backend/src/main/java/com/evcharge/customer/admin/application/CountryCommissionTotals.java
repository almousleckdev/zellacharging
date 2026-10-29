package com.evcharge.customer.admin.application;

import com.evcharge.customer.charging.CommissionTotals;
import com.evcharge.customer.shared.Country;

public record CountryCommissionTotals(Country country, CommissionTotals totals) {
}
