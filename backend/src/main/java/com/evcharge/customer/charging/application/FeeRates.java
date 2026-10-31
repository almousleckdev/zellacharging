package com.evcharge.customer.charging.application;

import java.math.BigDecimal;

public interface FeeRates {

    BigDecimal vatPercent();

    BigDecimal serviceFeePercent();
}
