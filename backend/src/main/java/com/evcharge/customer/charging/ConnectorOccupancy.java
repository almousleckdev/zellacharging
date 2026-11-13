package com.evcharge.customer.charging;

import java.util.Map;
import java.util.Set;

public interface ConnectorOccupancy {

    Map<String, Set<Integer>> busyConnectors();

    Map<String, Map<Integer, Integer>> minutesUntilFree();
}
