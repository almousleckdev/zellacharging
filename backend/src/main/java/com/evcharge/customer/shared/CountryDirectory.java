package com.evcharge.customer.shared;

import java.util.List;
import java.util.Optional;

public interface CountryDirectory {

    List<Country> active();

    Optional<Country> activeByCode(String code);

    Optional<Country> activeByPhoneRegion(String phoneRegion);

    Country defaultCountry();
}
