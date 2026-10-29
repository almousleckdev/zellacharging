package com.evcharge.customer.admin.application;

import com.evcharge.customer.catalog.StationInventory;
import com.evcharge.customer.charging.StationActivity;

public record StationView(StationInventory station, StationActivity activity) {
}
