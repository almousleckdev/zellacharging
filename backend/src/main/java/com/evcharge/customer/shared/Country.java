package com.evcharge.customer.shared;

public record Country(String code, String name, String currency, int currencyDecimals, String phoneRegion,
                      String timeZone) {
}
