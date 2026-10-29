package com.evcharge.customer.admin.application;

import com.evcharge.customer.shared.Country;

import java.util.List;

public record CountrySeries(Country country, List<SeriesPoint> points) {
}
