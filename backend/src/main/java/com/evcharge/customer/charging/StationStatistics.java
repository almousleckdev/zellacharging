package com.evcharge.customer.charging;

import java.util.Map;

public interface StationStatistics {

    Map<String, StationActivity> activity();
}
