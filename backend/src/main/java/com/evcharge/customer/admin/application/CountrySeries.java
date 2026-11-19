package com.evcharge.customer.admin.application;

import com.evcharge.customer.shared.Country;

import java.time.LocalDate;
import java.util.List;

public record CountrySeries(Country country, LocalDate today, List<SeriesPoint> points) {
}
