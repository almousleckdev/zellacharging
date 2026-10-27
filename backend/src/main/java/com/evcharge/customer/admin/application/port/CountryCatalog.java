package com.evcharge.customer.admin.application.port;

import com.evcharge.customer.shared.Country;

import java.util.List;

public interface CountryCatalog {
    List<Country> all();
}
