package com.evcharge.customer.admin.application;

import java.util.List;

public record AdminOverview(String scope, List<CountryOverview> countries) {
}
