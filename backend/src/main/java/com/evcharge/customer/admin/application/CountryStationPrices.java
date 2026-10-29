package com.evcharge.customer.admin.application;

import com.evcharge.customer.charging.StationPrice;
import com.evcharge.customer.shared.Country;

import java.util.List;

public record CountryStationPrices(Country country, List<StationPrice> prices) {
}
