package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.CountrySeries;
import com.evcharge.customer.admin.application.SeriesPoint;

import java.util.List;

record SeriesResponse(List<Country> countries) {

    static SeriesResponse from(List<CountrySeries> series) {
        return new SeriesResponse(series.stream().map(Country::from).toList());
    }

    record Country(String code, String name, String currency, int decimals, List<SeriesPoint> points) {

        static Country from(CountrySeries series) {
            var country = series.country();
            return new Country(country.code(), country.name(), country.currency(), country.currencyDecimals(),
                    series.points());
        }
    }
}
