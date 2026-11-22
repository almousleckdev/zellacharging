package com.evcharge.customer.partner;

import java.util.List;

public record FreeChargingUsePage(List<FreeChargingUse> items, int page, int size, long total) {
}
